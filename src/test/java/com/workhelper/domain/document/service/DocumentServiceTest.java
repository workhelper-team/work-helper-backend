package com.workhelper.domain.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.domain.consultation.entity.ConsultationMessage;
import com.workhelper.domain.consultation.entity.MessageRole;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.document.dto.DocumentUpdateRequest;
import com.workhelper.domain.document.entity.GeneratedDocument;
import com.workhelper.domain.document.repository.GeneratedDocumentRepository;
import com.workhelper.domain.evidence.entity.AnalysisStatus;
import com.workhelper.domain.evidence.entity.Evidence;
import com.workhelper.domain.evidence.repository.EvidenceRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import com.workhelper.infra.ai.AiClient;
import com.workhelper.infra.ai.AiIntegrationException;
import com.workhelper.infra.ai.dto.DocumentDraftAiRequest;
import com.workhelper.infra.ai.dto.DocumentDraftAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTest {
    private final LaborCaseRepository cases = mock(LaborCaseRepository.class);
    private final ConsultationMessageRepository consultations = mock(ConsultationMessageRepository.class);
    private final EvidenceRepository evidences = mock(EvidenceRepository.class);
    private final GeneratedDocumentRepository documents = mock(GeneratedDocumentRepository.class);
    private final AiClient ai = mock(AiClient.class);
    private final PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final ComplaintPdfGenerator pdfGenerator = new ComplaintPdfGenerator();
    private final DocumentService service = new DocumentService(cases, consultations, evidences, documents,
            ai, json, pdfGenerator, transactions);
    private LaborCase laborCase;

    @BeforeEach
    void setUp() {
        laborCase = LaborCase.builder().userId(7L).build();
        ReflectionTestUtils.setField(laborCase, "caseId", 3L);
        when(cases.findByCaseIdAndUserId(3L, 7L)).thenReturn(Optional.of(laborCase));
        when(transactions.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        when(consultations.findByLaborCase_CaseIdOrderByCreatedAtAsc(3L)).thenReturn(List.of(
                ConsultationMessage.builder().laborCase(laborCase).role(MessageRole.USER).content("질문").build(),
                ConsultationMessage.builder().laborCase(laborCase).role(MessageRole.ASSISTANT).content("답변").build()));
        when(ai.draftDocument(any())).thenReturn(draft());
        when(documents.saveAndFlush(any())).thenAnswer(invocation -> {
            GeneratedDocument document = invocation.getArgument(0);
            ReflectionTestUtils.setField(document, "documentId", 10L);
            return document;
        });
    }

    @Test
    void createsFromHistoryAndLatestEvidenceAndStoresNullableForm() {
        Evidence evidence = Evidence.builder().laborCase(laborCase).analysisStatus(AnalysisStatus.COMPLETED).build();
        evidence.updateAnalysisResult("original", json.createObjectNode().put("analysisSummary", "분석"),
                AnalysisStatus.COMPLETED);
        evidence.updateExtractedText("사용자 수정 OCR");
        when(evidences.findByLaborCase_CaseId(3L)).thenReturn(List.of(evidence));

        var result = service.create(7L, 3L);

        ArgumentCaptor<DocumentDraftAiRequest> request = ArgumentCaptor.forClass(DocumentDraftAiRequest.class);
        verify(ai).draftDocument(request.capture());
        assertThat(request.getValue().chatHistory()).extracting(DocumentDraftAiRequest.ChatMessage::role)
                .containsExactly(DocumentDraftAiRequest.Role.USER, DocumentDraftAiRequest.Role.ASSISTANT);
        assertThat(request.getValue().evidenceDocument().extractedText()).isEqualTo("사용자 수정 OCR");
        assertThat(request.getValue().evidenceDocument().analysisSummary()).isEqualTo("분석");
        ArgumentCaptor<GeneratedDocument> saved = ArgumentCaptor.forClass(GeneratedDocument.class);
        verify(documents).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getDocumentType()).isEqualTo("COMPLAINT");
        assertThat(saved.getValue().getContent()).isEqualTo("진정 사유");
        assertThat(saved.getValue().getFormData().path("complainant").path("name").isNull()).isTrue();
        assertThat(saved.getValue().getFormData().path("respondent").path("companyName").isNull()).isTrue();
        assertThat(saved.getValue().getFormData().path("facts").path("hireDate").isNull()).isTrue();
        assertThat(saved.getValue().getFormData().path("content").path("targetLaborOffice").asText())
                .isEqualTo("서울청");
        assertThat(saved.getValue().getFormData().path("content").has("claimReason")).isFalse();
        assertThat(result.content().claimReason()).isEqualTo("진정 사유");
        InOrder order = inOrder(transactions, ai);
        order.verify(transactions).commit(any());
        order.verify(ai).draftDocument(any());
        order.verify(transactions).commit(any());
    }

    @Test
    void noEvidenceSendsNullAndDetailRestoresStoredData() {
        var created = service.create(7L, 3L);
        ArgumentCaptor<DocumentDraftAiRequest> request = ArgumentCaptor.forClass(DocumentDraftAiRequest.class);
        verify(ai).draftDocument(request.capture());
        assertThat(request.getValue().evidenceDocument()).isNull();
        when(documents.findByDocumentIdAndLaborCase_CaseId(10L, 3L))
                .thenReturn(Optional.of(savedDocument(created.content().claimReason())));
        var loaded = service.get(7L, 3L, 10L);
        assertThat(loaded.content().claimReason()).isEqualTo("진정 사유");
        assertThat(loaded.content().targetLaborOffice()).isEqualTo("서울청");
        assertThat(loaded.content().totalUnpaidAmount()).isEqualByComparingTo("100");
    }

    @Test
    void patchStoresUserValuesWithoutCallingAiAndKeepsType() {
        GeneratedDocument existing = savedDocument("old");
        when(documents.findByDocumentIdAndLaborCase_CaseId(10L, 3L)).thenReturn(Optional.of(existing));
        var request = new DocumentUpdateRequest("수정 제목", json.createObjectNode().put("name", "홍길동"),
                json.createObjectNode().put("companyName", "회사"), json.createObjectNode().put("payDay", "25"),
                new DocumentUpdateRequest.Content("수정 사유", "부산청", new BigDecimal("500")));

        var result = service.update(7L, 3L, 10L, request);

        assertThat(existing.getContent()).isEqualTo("수정 사유");
        assertThat(existing.getFormData().path("complainant").path("name").asText()).isEqualTo("홍길동");
        assertThat(result.title()).isEqualTo("수정 제목");
        assertThat(result.content().targetLaborOffice()).isEqualTo("부산청");
        assertThat(result.documentType()).isEqualTo("COMPLAINT");
        verifyNoInteractions(ai);
    }

    @Test
    void rejectsOtherCaseAndOtherOwner() {
        assertThatThrownBy(() -> service.get(7L, 3L, 99L)).isInstanceOf(IllegalArgumentException.class);
        verify(documents).findByDocumentIdAndLaborCase_CaseId(99L, 3L);
        assertThatThrownBy(() -> service.get(8L, 3L, 10L)).isInstanceOf(IllegalArgumentException.class);
        verify(documents, never()).findByDocumentIdAndLaborCase_CaseId(10L, 3L);
        var update = new DocumentUpdateRequest(null, json.createObjectNode(), json.createObjectNode(),
                json.createObjectNode(), new DocumentUpdateRequest.Content("사유", null, null));
        assertThatThrownBy(() -> service.update(8L, 3L, 10L, update))
                .isInstanceOf(IllegalArgumentException.class);
        verify(documents, never()).flush();
    }

    @Test
    void storesWholeNullAiSectionsWithoutInventingValues() {
        when(ai.draftDocument(any())).thenReturn(new DocumentDraftAiResponse(true, 3L, null, null, null,
                new DocumentDraftAiResponse.Content("사유", null, null)));
        var result = service.create(7L, 3L);
        assertThat(result.complainant().isNull()).isTrue();
        assertThat(result.respondent().isNull()).isTrue();
        assertThat(result.facts().isNull()).isTrue();
        assertThat(result.content().totalUnpaidAmount()).isNull();
    }

    @Test
    void missingHistoryAndAiFailureSaveNothing() {
        when(consultations.findByLaborCase_CaseIdOrderByCreatedAtAsc(3L)).thenReturn(List.of());
        assertThatThrownBy(() -> service.create(7L, 3L)).isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(ai);
        verify(documents, never()).saveAndFlush(any());

        when(consultations.findByLaborCase_CaseIdOrderByCreatedAtAsc(3L)).thenReturn(List.of(
                ConsultationMessage.builder().role(MessageRole.USER).content("질문").build()));
        when(ai.draftDocument(any())).thenThrow(new AiIntegrationException(
                AiIntegrationException.Kind.TIMEOUT, null, "timeout", null));
        assertThatThrownBy(() -> service.create(7L, 3L)).isInstanceOf(AiIntegrationException.class);
        verify(documents, never()).saveAndFlush(any());
    }

    @Test
    void pdfUsesLatestSavedValuesAndSupportsNullFieldsWithoutAi() throws Exception {
        GeneratedDocument existing = savedDocument("이전 진정 사유");
        when(documents.findByDocumentIdAndLaborCase_CaseId(10L, 3L)).thenReturn(Optional.of(existing));
        var request = new DocumentUpdateRequest("진정서", json.createObjectNode().put("name", "홍길동")
                .putNull("birthDate"), json.createObjectNode().putNull("companyName"),
                json.createObjectNode().put("unpaidWages", 1500000),
                new DocumentUpdateRequest.Content("수정된 진정 사유", null, new BigDecimal("1500000")));
        service.update(7L, 3L, 10L, request);

        byte[] bytes = service.getPdf(7L, 3L, 10L);

        assertThat(bytes).isNotEmpty();
        assertThat(new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
        try (var pdf = org.apache.pdfbox.Loader.loadPDF(bytes)) {
            String text = new org.apache.pdfbox.text.PDFTextStripper().getText(pdf);
            assertThat(text).contains("홍길동", "수정된 진정 사유", "1,500,000원");
            assertThat(text).doesNotContain("이전 진정 사유");
        }
        verifyNoInteractions(ai);
    }

    @Test
    void pdfRejectsOtherOwnerAndMismatchedDocument() {
        assertThatThrownBy(() -> service.getPdf(8L, 3L, 10L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getPdf(7L, 3L, 99L)).isInstanceOf(IllegalArgumentException.class);
        verify(documents, never()).findByDocumentIdAndLaborCase_CaseId(10L, 3L);
        verify(documents).findByDocumentIdAndLaborCase_CaseId(99L, 3L);
        verifyNoInteractions(ai);
    }

    @Test
    void pdfRejectsUnsupportedDocumentType() {
        GeneratedDocument existing = savedDocument("사유");
        ReflectionTestUtils.setField(existing, "documentType", "OTHER");
        when(documents.findByDocumentIdAndLaborCase_CaseId(10L, 3L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.getPdf(7L, 3L, 10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported document type");
        verifyNoInteractions(ai);
    }

    private DocumentDraftAiResponse draft() {
        return new DocumentDraftAiResponse(true, 3L,
                new DocumentDraftAiResponse.Complainant(null, null, null, null, null, null, null),
                new DocumentDraftAiResponse.Respondent(null, null, null, null, null, null),
                new DocumentDraftAiResponse.Facts(null, null, null, null, null, null, null,
                        null, null),
                new DocumentDraftAiResponse.Content("진정 사유", "서울청", new BigDecimal("100")));
    }

    private GeneratedDocument savedDocument(String claimReason) {
        var form = json.createObjectNode();
        form.set("complainant", json.createObjectNode().put("name", "홍길동"));
        form.set("respondent", json.createObjectNode());
        form.set("facts", json.createObjectNode());
        form.set("content", json.createObjectNode().put("targetLaborOffice", "서울청")
                .put("totalUnpaidAmount", 100));
        GeneratedDocument document = new GeneratedDocument(laborCase, null, claimReason, form);
        ReflectionTestUtils.setField(document, "documentId", 10L);
        return document;
    }
}
