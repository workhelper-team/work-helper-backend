package com.workhelper.domain.document.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.workhelper.domain.consultation.entity.MessageRole;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.document.dto.DocumentDetailResponse;
import com.workhelper.domain.document.dto.DocumentSummaryResponse;
import com.workhelper.domain.document.dto.DocumentUpdateRequest;
import com.workhelper.domain.document.entity.GeneratedDocument;
import com.workhelper.domain.document.repository.GeneratedDocumentRepository;
import com.workhelper.domain.evidence.entity.Evidence;
import com.workhelper.domain.evidence.repository.EvidenceRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import com.workhelper.infra.ai.AiClient;
import com.workhelper.infra.ai.AiIntegrationException;
import com.workhelper.infra.ai.dto.DocumentDraftAiRequest;
import com.workhelper.infra.ai.dto.DocumentDraftAiResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DocumentService {
    private final LaborCaseRepository cases;
    private final ConsultationMessageRepository consultations;
    private final EvidenceRepository evidences;
    private final GeneratedDocumentRepository documents;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    private final ComplaintPdfGenerator pdfGenerator;
    private final TransactionTemplate readTransaction;
    private final TransactionTemplate writeTransaction;

    public DocumentService(LaborCaseRepository cases, ConsultationMessageRepository consultations,
                           EvidenceRepository evidences, GeneratedDocumentRepository documents,
                           AiClient aiClient, ObjectMapper objectMapper, ComplaintPdfGenerator pdfGenerator,
                           PlatformTransactionManager transactionManager) {
        this.cases = cases;
        this.consultations = consultations;
        this.evidences = evidences;
        this.documents = documents;
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
        this.pdfGenerator = pdfGenerator;
        this.readTransaction = new TransactionTemplate(transactionManager);
        this.readTransaction.setReadOnly(true);
        this.readTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.writeTransaction = new TransactionTemplate(transactionManager);
        this.writeTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public DocumentDetailResponse create(Long userId, Long caseId) {
        DocumentDraftAiRequest request = readTransaction.execute(status -> buildDraftRequest(userId, caseId));
        DocumentDraftAiResponse draft = aiClient.draftDocument(request);
        if (!Boolean.TRUE.equals(draft.success()) || !caseId.equals(draft.caseId())
                || draft.content() == null || draft.content().claimReason() == null) {
            throw new AiIntegrationException(AiIntegrationException.Kind.INVALID_RESPONSE, null,
                    "AI server returned an incomplete document draft", null);
        }
        return writeTransaction.execute(status -> {
            LaborCase laborCase = ownedCase(userId, caseId);
            GeneratedDocument saved = documents.saveAndFlush(new GeneratedDocument(laborCase, null,
                    draft.content().claimReason(), formData(draft.complainant(), draft.respondent(),
                    draft.facts(), draft.content().targetLaborOffice(), draft.content().totalUnpaidAmount())));
            return detail(saved);
        });
    }

    public List<DocumentSummaryResponse> list(Long userId, Long caseId) {
        return readTransaction.execute(status -> {
            ownedCase(userId, caseId);
            return documents.findByLaborCase_CaseIdOrderByCreatedAtDesc(caseId).stream()
                    .map(document -> new DocumentSummaryResponse(document.getDocumentId(), caseId,
                            document.getDocumentType(), document.getTitle(), document.getCreatedAt(),
                            document.getUpdatedAt())).toList();
        });
    }

    public DocumentDetailResponse get(Long userId, Long caseId, Long documentId) {
        return readTransaction.execute(status -> {
            ownedCase(userId, caseId);
            return detail(findDocument(caseId, documentId));
        });
    }

    public byte[] getPdf(Long userId, Long caseId, Long documentId) {
        DocumentDetailResponse document = get(userId, caseId, documentId);
        if (!GeneratedDocument.COMPLAINT.equals(document.documentType())) {
            throw new IllegalArgumentException("Unsupported document type: " + document.documentType());
        }
        return pdfGenerator.generate(document);
    }

    public DocumentDetailResponse update(Long userId, Long caseId, Long documentId,
                                         DocumentUpdateRequest request) {
        return writeTransaction.execute(status -> {
            ownedCase(userId, caseId);
            GeneratedDocument document = findDocument(caseId, documentId);
            document.update(request.title(), request.content().claimReason(),
                    formData(request.complainant(), request.respondent(), request.facts(),
                            request.content().targetLaborOffice(), request.content().totalUnpaidAmount()));
            documents.flush();
            return detail(document);
        });
    }

    private DocumentDraftAiRequest buildDraftRequest(Long userId, Long caseId) {
        ownedCase(userId, caseId);
        List<DocumentDraftAiRequest.ChatMessage> history = consultations
                .findByLaborCase_CaseIdOrderByCreatedAtAsc(caseId).stream()
                .map(message -> new DocumentDraftAiRequest.ChatMessage(
                        message.getRole() == MessageRole.USER ? DocumentDraftAiRequest.Role.USER
                                : DocumentDraftAiRequest.Role.ASSISTANT, message.getContent()))
                .toList();
        if (history.isEmpty()) {
            throw new IllegalArgumentException("Document draft requires consultation history");
        }
        DocumentDraftAiRequest.EvidenceDocument evidenceDocument = evidences.findByLaborCase_CaseId(caseId)
                .stream().filter(evidence -> evidence.getAnalysisResult() != null
                        && evidence.getAnalysisResult().path("analysisSummary").isTextual())
                .findFirst().map(this::evidenceDocument).orElse(null);
        return new DocumentDraftAiRequest(caseId, history, evidenceDocument);
    }

    private DocumentDraftAiRequest.EvidenceDocument evidenceDocument(Evidence evidence) {
        return new DocumentDraftAiRequest.EvidenceDocument(evidence.getExtractedText(),
                evidence.getAnalysisResult().path("analysisSummary").asText());
    }

    private ObjectNode formData(Object complainant, Object respondent, Object facts,
                                String targetLaborOffice, BigDecimal totalUnpaidAmount) {
        ObjectNode form = objectMapper.createObjectNode();
        form.set("complainant", objectMapper.valueToTree(complainant));
        form.set("respondent", objectMapper.valueToTree(respondent));
        form.set("facts", objectMapper.valueToTree(facts));
        ObjectNode content = objectMapper.createObjectNode();
        content.set("targetLaborOffice", objectMapper.valueToTree(targetLaborOffice));
        content.set("totalUnpaidAmount", objectMapper.valueToTree(totalUnpaidAmount));
        form.set("content", content);
        return form;
    }

    private DocumentDetailResponse detail(GeneratedDocument document) {
        JsonNode form = document.getFormData();
        JsonNode content = form.path("content");
        return new DocumentDetailResponse(document.getDocumentId(), document.getLaborCase().getCaseId(),
                document.getDocumentType(), document.getTitle(), field(form, "complainant"),
                field(form, "respondent"), field(form, "facts"),
                new DocumentDetailResponse.Content(document.getContent(), text(content, "targetLaborOffice"),
                        decimal(content, "totalUnpaidAmount")), document.getCreatedAt(), document.getUpdatedAt());
    }

    private JsonNode field(JsonNode form, String name) {
        return form.has(name) ? form.get(name) : null;
    }

    private String text(JsonNode node, String name) {
        JsonNode value = node.path(name);
        return value.isNull() || value.isMissingNode() ? null : value.asText();
    }

    private BigDecimal decimal(JsonNode node, String name) {
        JsonNode value = node.path(name);
        return value.isNumber() ? value.decimalValue() : null;
    }

    private LaborCase ownedCase(Long userId, Long caseId) {
        return cases.findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found: " + caseId));
    }

    private GeneratedDocument findDocument(Long caseId, Long documentId) {
        return documents.findByDocumentIdAndLaborCase_CaseId(documentId, caseId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }
}
