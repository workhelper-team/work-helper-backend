package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.client.AiAnalysisRequest;
import com.workhelper.domain.evidence.client.AiAnalysisResult;
import com.workhelper.domain.evidence.client.AiEvidenceAnalysisClient;
import com.workhelper.domain.evidence.dto.EvidenceAnalysisResponse;
import com.workhelper.domain.evidence.dto.EvidenceDeleteResponse;
import com.workhelper.domain.evidence.dto.EvidenceDetailResponse;
import com.workhelper.domain.evidence.dto.EvidenceSummaryResponse;
import com.workhelper.domain.evidence.dto.EvidenceUploadResponse;
import com.workhelper.domain.evidence.entity.AnalysisStatus;
import com.workhelper.domain.evidence.entity.Evidence;
import com.workhelper.domain.evidence.exception.EvidenceExceptions.CaseAccessDeniedException;
import com.workhelper.domain.evidence.exception.EvidenceExceptions.CaseNotFoundException;
import com.workhelper.domain.evidence.exception.EvidenceExceptions.EvidenceAnalysisFailedException;
import com.workhelper.domain.evidence.exception.EvidenceExceptions.EvidenceNotFoundException;
import com.workhelper.domain.evidence.exception.EvidenceExceptions.InvalidEvidenceFileException;
import com.workhelper.domain.evidence.repository.EvidenceRepository;

import com.workhelper.domain.laborcase.entity.Case;
import com.workhelper.domain.laborcase.repository.CaseRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

    /** 허용 MIME 타입은 확장자가 아니라 업로드 요청의 Content-Type을 기준으로 검사한다. */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/png", "image/jpeg", "image/jpg");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final EvidenceRepository evidenceRepository;
    private final CaseRepository caseRepository;
    private final EvidenceFailureRecorder evidenceFailureRecorder;
    private final EvidenceStorageService storageService;
    private final AiEvidenceAnalysisClient aiEvidenceAnalysisClient;

    @Override
    @Transactional
    public EvidenceUploadResponse uploadEvidence(Long caseId, MultipartFile file, String description, Long requestUserId) {
        // 1. 사건을 조회한다. 사건이 없으면 파일을 저장하기 전에 즉시 종료한다.
        // 파일을 저장하기 전에 사건 존재 여부와 요청 사용자의 소유권을 확인한다.
        // 저장소와 DB 작업 중 하나만 성공하면 불일치가 생길 수 있으므로, 운영 S3 구현에서는
        // 실패 시 보상 삭제 또는 업로드 후 DB 저장 정책을 별도로 정의해야 한다.
        Case targetCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException(caseId));

        // 2. 다른 사용자의 사건에 파일을 올리지 못하도록 소유권을 확인한다.
        validateOwnership(targetCase, requestUserId);
        // 3. 빈 파일, 허용하지 않은 형식, 너무 큰 파일을 차단한다.
        validateFile(file);

        // 4. 파일을 로컬 디스크 또는 S3에 저장하고, 저장 위치를 반환받는다.
        String storagePath = storageService.store(file, caseId);

        // 현재 description은 API 입력으로 받지만 Evidence 엔티티에 저장할 필드가 없어 버려진다.
        // 제품 요구사항에서 설명을 보존해야 한다면 엔티티, 마이그레이션, 응답 DTO를 함께 추가해야 한다.
        Evidence evidence = Evidence.builder()
                .targetCase(targetCase)
                .originalName(file.getOriginalFilename())
                .storagePath(storagePath)
                .mimeType(file.getContentType())
                .build();

        // 5. 파일 위치와 분석 상태만 DB에 저장한다. 이미지 바이너리 자체는 DB에 저장하지 않는다.
        Evidence saved = evidenceRepository.save(evidence);

        return new EvidenceUploadResponse(
                saved.getEvidenceId(),
                saved.getOriginalName(),
                saved.getMimeType(),
                saved.getAnalysisStatus().name()
        );
    }

    private void validateOwnership(Case targetCase, Long requestUserId) {
        if (!targetCase.getUser().getUserId().equals(requestUserId)) {
            throw new CaseAccessDeniedException(targetCase.getCaseId());
        }
    }

    private void validateFile(MultipartFile file) {
        // MIME 타입만으로는 악성 파일의 실제 형식을 완전히 검증할 수 없다.
        // 운영 환경에서는 파일 시그니처 검사와 S3 업로드 후 바이러스 검사도 고려한다.
        if (file == null || file.isEmpty()) {
            throw new InvalidEvidenceFileException("업로드할 파일이 없습니다.");
        }
        if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new InvalidEvidenceFileException("지원하지 않는 파일 형식입니다: " + file.getContentType());
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidEvidenceFileException("파일 크기가 제한(10MB)을 초과했습니다.");
        }
    }

    @Override
    @Transactional
    public EvidenceAnalysisResponse analyzeEvidence(Long caseId, Long evidenceId, Long requestUserId) {
        // 사건 ID와 증거 ID를 함께 조건으로 사용해 다른 사건의 증거가 조회되지 않도록 한다.
        Evidence evidence = evidenceRepository.findByEvidenceIdAndTargetCase_CaseId(evidenceId, caseId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));

        validateOwnership(evidence.getTargetCase(), requestUserId);

        AiAnalysisResult result;
        try {
            // AI 서버 호출은 외부 시스템 의존성을 가지므로, 실패한 분석을 별도 트랜잭션에서
            // FAILED로 기록한 뒤 표준 502 오류로 변환한다.
            result = aiEvidenceAnalysisClient.analyze(
                    new AiAnalysisRequest(evidence.getEvidenceId(), evidence.getStoragePath(), evidence.getMimeType())
            );
        } catch (Exception e) {
            evidenceFailureRecorder.markAsFailed(evidenceId);
            throw new EvidenceAnalysisFailedException(evidenceId, e);
        }

        // AI가 반환한 결과를 엔티티에 반영한다. 트랜잭션이 끝나면 JPA가 변경 내용을 UPDATE한다.
        evidence.applyAnalysisResult(result.extractedText(), result.analysisResultJson(), AnalysisStatus.COMPLETED);

        return new EvidenceAnalysisResponse(
                evidence.getEvidenceId(),
                evidence.getExtractedText(),
                evidence.getAnalysisResult(),
                evidence.getAnalysisStatus().name()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceSummaryResponse> getEvidences(Long caseId, Long requestUserId) {
        // 목록 조회도 먼저 사건 소유권을 확인한 뒤 해당 사건의 증거만 반환한다.
        Case targetCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new CaseNotFoundException(caseId));
        validateOwnership(targetCase, requestUserId);

        return evidenceRepository.findByTargetCase_CaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(e -> new EvidenceSummaryResponse(
                        e.getEvidenceId(),
                        e.getOriginalName(),
                        e.getMimeType(),
                        e.getAnalysisStatus().name(),
                        e.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceDetailResponse getEvidenceDetail(Long caseId, Long evidenceId, Long requestUserId) {
        Evidence evidence = evidenceRepository.findByEvidenceIdAndTargetCase_CaseId(evidenceId, caseId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));
        validateOwnership(evidence.getTargetCase(), requestUserId);

        return new EvidenceDetailResponse(
                evidence.getEvidenceId(),
                evidence.getOriginalName(),
                evidence.getMimeType(),
                evidence.getExtractedText(),
                evidence.getAnalysisResult(),
                evidence.getAnalysisStatus().name(),
                evidence.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public EvidenceDeleteResponse deleteEvidence(Long caseId, Long evidenceId, Long requestUserId) {
        // DB 레코드 삭제 전에 파일을 삭제한다. S3 전환 시 삭제 실패 정책(재시도 큐 또는
        // 고아 객체 정리 작업)을 정하지 않으면 DB와 저장소가 서로 다른 상태가 될 수 있다.
        Evidence evidence = evidenceRepository.findByEvidenceIdAndTargetCase_CaseId(evidenceId, caseId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));
        validateOwnership(evidence.getTargetCase(), requestUserId);

        storageService.delete(evidence.getStoragePath());
        evidenceRepository.delete(evidence);

        return new EvidenceDeleteResponse(evidenceId, "DELETED");
    }
}