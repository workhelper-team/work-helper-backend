package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.dto.EvidenceUploadResponse;
import org.springframework.web.multipart.MultipartFile;
import com.workhelper.domain.evidence.dto.EvidenceAnalysisResponse;
import com.workhelper.domain.evidence.dto.EvidenceDeleteResponse;
import com.workhelper.domain.evidence.dto.EvidenceDetailResponse;
import com.workhelper.domain.evidence.dto.EvidenceSummaryResponse;
import java.util.List; // java.util.List 임포트 추가

public interface EvidenceService {

    // 1. 위쪽에 있던 메서드를 인터페이스 안으로 이동
    EvidenceAnalysisResponse analyzeEvidence(Long caseId, Long evidenceId, Long requestUserId);

    /**
     * API-EVD-001: 증거 이미지 업로드
     * ...
     */
    EvidenceUploadResponse uploadEvidence(Long caseId, MultipartFile file, String description, Long requestUserId);

    // 증거 목록 조회
    List<EvidenceSummaryResponse> getEvidences(Long caseId, Long requestUserId);

    // 증거 상세 조회
    EvidenceDetailResponse getEvidenceDetail(Long caseId, Long evidenceId, Long requestUserId);

    // 증거 삭제
    EvidenceDeleteResponse deleteEvidence(Long caseId, Long evidenceId, Long requestUserId);
}
