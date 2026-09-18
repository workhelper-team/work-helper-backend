package com.workhelper.domain.evidence.service;

import com.workhelper.domain.evidence.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface EvidenceService {

    EvidenceUploadResponse uploadEvidence(Long caseId, MultipartFile file, String description);

    EvidenceAnalysisResponse analyzeEvidence(Long caseId, Long evidenceId);

    Page<EvidenceSummaryResponse> getEvidences(Long caseId, int page, int size);

    EvidenceDetailResponse getEvidenceDetail(Long caseId, Long evidenceId);

    void deleteEvidence(Long caseId, Long evidenceId);
}