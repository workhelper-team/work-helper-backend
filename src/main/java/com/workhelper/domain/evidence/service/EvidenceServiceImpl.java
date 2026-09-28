package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.dto.*;
import com.workhelper.domain.evidence.entity.AnalysisStatus;
import com.workhelper.domain.evidence.entity.Evidence;
import com.workhelper.domain.evidence.repository.EvidenceRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import com.workhelper.infra.ai.AiClient;
import com.workhelper.infra.ai.AiIntegrationException;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiRequest;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiResponse;
import com.workhelper.infra.storage.EvidenceStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
/**
 * Evidence의 업무 흐름을 조정합니다.
 * 사건 확인, Storage 처리, Evidence DB 처리를 한 서비스에서 순서대로 관리합니다.
 */
public class EvidenceServiceImpl implements EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final LaborCaseRepository laborCaseRepository;
    private final EvidenceStorageService evidenceStorageService;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public EvidenceServiceImpl(EvidenceRepository evidenceRepository,
                               LaborCaseRepository laborCaseRepository,
                               EvidenceStorageService evidenceStorageService,
                               AiClient aiClient,
                               ObjectMapper objectMapper,
                               PlatformTransactionManager transactionManager) {
        this.evidenceRepository = evidenceRepository;
        this.laborCaseRepository = laborCaseRepository;
        this.evidenceStorageService = evidenceStorageService;
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }
    /** Storage에 파일을 저장한 뒤 반환된 Object Key와 메타데이터를 DB에 저장합니다. */
    @Override
    @Transactional
    public EvidenceUploadResponse uploadEvidence(Long userId, Long caseId, MultipartFile file, String description) {
        // 1. 먼저 연결할 사건이 실제로 존재하는지 확인합니다.
        LaborCase laborCase = laborCaseRepository.findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사건입니다. caseId=" + caseId));

        // 2. 파일 본문은 Storage에 저장하고 DB에 기록할 Object Key를 받습니다.
        String objectKey = evidenceStorageService.save(caseId, file);

        // 3. Object Key와 업로드 정보를 Evidence로 저장합니다.
        Evidence evidence = Evidence.builder()
                .laborCase(laborCase)
                .originalName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .description(description)
                .storagePath(objectKey) // URL이 아닌 Object Key 저장
                .analysisStatus(AnalysisStatus.PENDING)
                .build();

        Evidence savedEvidence;
        try {
            savedEvidence = evidenceRepository.saveAndFlush(evidence);
        } catch (RuntimeException databaseException) {
            try {
                evidenceStorageService.delete(objectKey);
            } catch (RuntimeException cleanupException) {
                databaseException.addSuppressed(cleanupException);
            }
            throw databaseException;
        }

        return new EvidenceUploadResponse(
                savedEvidence.getEvidenceId(),
                savedEvidence.getOriginalName(),
                savedEvidence.getMimeType(),
                savedEvidence.getDescription(),
                savedEvidence.getAnalysisStatus().name(),
                savedEvidence.getCreatedAt()
        );
    }

    /** 분석 상태를 먼저 확정한 뒤 AI 호출과 결과 저장을 별도 트랜잭션에서 처리합니다. */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public EvidenceAnalysisResponse analyzeEvidence(Long userId, Long caseId, Long evidenceId) {
        AnalysisContext context = transactionTemplate.execute(status -> {
            verifyCaseOwnership(userId, caseId);
            Evidence evidence = findEvidence(caseId, evidenceId);
            evidence.updateAnalysisStatus(AnalysisStatus.PROCESSING);
            return new AnalysisContext(evidence.getStoragePath(), evidence.getDescription());
        });

        try {
            String fileUrl = evidenceStorageService.getPresignedGetUrl(context.storagePath());
            String userContext = StringUtils.hasText(context.description()) ? context.description() : "";
            EvidenceAnalysisAiResponse result = aiClient.analyzeEvidence(
                    new EvidenceAnalysisAiRequest(fileUrl, userContext));
            if (result.extractedText() == null || result.analysisSummary() == null) {
                throw new AiIntegrationException(AiIntegrationException.Kind.INVALID_RESPONSE, null,
                        "AI server returned an incomplete evidence analysis", null);
            }

            return transactionTemplate.execute(status -> {
                Evidence evidence = findEvidence(caseId, evidenceId);
                evidence.updateAnalysisResult(result.extractedText(),
                        objectMapper.createObjectNode().put("analysisSummary", result.analysisSummary()),
                        AnalysisStatus.COMPLETED);
                return new EvidenceAnalysisResponse(evidence.getEvidenceId(), evidence.getExtractedText(),
                        evidence.getAnalysisResult(), evidence.getAnalysisStatus().name());
            });
        } catch (RuntimeException exception) {
            try {
                transactionTemplate.executeWithoutResult(status ->
                        findEvidence(caseId, evidenceId).updateAnalysisStatus(AnalysisStatus.FAILED));
            } catch (RuntimeException updateException) {
                exception.addSuppressed(updateException);
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public EvidenceDetailResponse updateExtractedText(Long userId, Long caseId, Long evidenceId,
                                                       String extractedText) {
        verifyCaseOwnership(userId, caseId);
        Evidence evidence = findEvidence(caseId, evidenceId);
        if (evidence.getAnalysisStatus() != AnalysisStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Evidence OCR text can only be updated after analysis is completed");
        }
        evidence.updateExtractedText(extractedText);
        return toDetailResponse(evidence);
    }

    private record AnalysisContext(String storagePath, String description) {
    }

    /** 사건에 속한 Evidence를 페이징 조회하고 목록 응답으로 변환합니다. */
    @Override
    public Page<EvidenceSummaryResponse> getEvidences(Long userId, Long caseId, int page, int size) {
        verifyCaseOwnership(userId, caseId);
        PageRequest pageable = PageRequest.of(page, size);
        Page<Evidence> evidencePage = evidenceRepository.findByLaborCase_CaseId(caseId, pageable);

        return evidencePage.map(evidence -> new EvidenceSummaryResponse(
                evidence.getEvidenceId(),
                evidence.getOriginalName(),
                evidence.getMimeType(),
                evidence.getDescription(),
                evidence.getAnalysisStatus().name(),
                evidence.getCreatedAt()
        ));
    }

    /** Evidence 메타데이터와 Storage 접근용 fileUrl을 함께 반환합니다. */
    @Override
    public EvidenceDetailResponse getEvidenceDetail(Long userId, Long caseId, Long evidenceId) {
        verifyCaseOwnership(userId, caseId);
        Evidence evidence = findEvidence(caseId, evidenceId);

        return toDetailResponse(evidence);
    }

    private EvidenceDetailResponse toDetailResponse(Evidence evidence) {

        // DB에는 Object Key만 있으므로 응답용 접근 경로는 Storage에서 생성합니다.
        String fileUrl = evidenceStorageService.getFileUrl(evidence.getStoragePath());

        return new EvidenceDetailResponse(
                evidence.getEvidenceId(),
                evidence.getOriginalName(),
                evidence.getMimeType(),
                evidence.getDescription(),
                fileUrl,
                evidence.getExtractedText(),
                evidence.getAnalysisResult(), // JsonNode 타입
                evidence.getAnalysisStatus().name(),
                evidence.getCreatedAt()
        );
    }

    /** 소유권 확인 후 Storage 파일과 DB Evidence를 순서대로 물리 삭제합니다. */
    @Override
    @Transactional
    public void deleteEvidence(Long userId, Long caseId, Long evidenceId) {
        verifyCaseOwnership(userId, caseId);
        // 1. 사건과 Evidence의 연결을 확인해 다른 사건의 파일을 삭제하지 않도록 합니다.
        Evidence evidence = evidenceRepository.findByEvidenceIdAndLaborCase_CaseId(evidenceId, caseId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없거나 존재하지 않는 증거입니다."));

        // 2. DB에 저장된 Object Key로 실제 파일을 먼저 삭제합니다.
        evidenceStorageService.delete(evidence.getStoragePath());

        // 3. Storage 삭제가 성공한 경우에만 DB 레코드를 삭제합니다.
        evidenceRepository.delete(evidence);
    }

    private LaborCase verifyCaseOwnership(Long userId, Long caseId) {
        return laborCaseRepository.findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사건입니다. caseId=" + caseId));
    }

    private Evidence findEvidence(Long caseId, Long evidenceId) {
        return evidenceRepository.findByEvidenceIdAndLaborCase_CaseId(evidenceId, caseId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found for case"));
    }
}
