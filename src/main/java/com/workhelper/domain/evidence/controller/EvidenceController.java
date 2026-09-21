package com.workhelper.domain.evidence.controller;

import com.workhelper.domain.evidence.dto.*;
import com.workhelper.domain.evidence.service.EvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cases/{caseId}/evidences")
@RequiredArgsConstructor
/**
 * Evidence 외부 API의 입구입니다.
 * 요청 형식 검증과 응답 형식 변환은 Controller가 담당하고, 실제 처리는 EvidenceService에 위임합니다.
 */
public class EvidenceController {

    private final EvidenceService evidenceService;

    // Spring Page를 API 규격에 맞는 일반 응답 객체로 변환합니다.
    public record PageResult<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean last
    ) {
        public static <T> PageResult<T> from(Page<T> pageData) {
            return new PageResult<>(
                    pageData.getContent(),
                    pageData.getNumber(),
                    pageData.getSize(),
                    pageData.getTotalElements(),
                    pageData.getTotalPages(),
                    pageData.isLast()
            );
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceUploadResponse> uploadEvidence(
            @PathVariable Long caseId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description
    ) {
        // 업로드 정책: 비어 있지 않은 JPEG/PNG 파일만 허용합니다.
        if (file.isEmpty() || (!"image/jpeg".equals(file.getContentType()) && !"image/png".equals(file.getContentType()))) {
            throw new IllegalArgumentException("JPEG 또는 PNG 형식의 이미지 파일만 업로드 가능합니다.");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 최대 10MB를 초과할 수 없습니다.");
        }

        EvidenceUploadResponse response = evidenceService.uploadEvidence(caseId, file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{evidenceId}/analysis")
    public ResponseEntity<EvidenceAnalysisResponse> analyzeEvidence(
            @PathVariable Long caseId,
            @PathVariable Long evidenceId
    ) {
        EvidenceAnalysisResponse response = evidenceService.analyzeEvidence(caseId, evidenceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResult<EvidenceSummaryResponse>> getEvidences(
            @PathVariable Long caseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);
        Page<EvidenceSummaryResponse> pageData = evidenceService.getEvidences(caseId, page, size);
        return ResponseEntity.ok(PageResult.from(pageData));
    }

    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page는 0 이상, size는 1 이상 100 이하이어야 합니다.");
        }
    }

    @GetMapping("/{evidenceId}")
    public ResponseEntity<EvidenceDetailResponse> getEvidenceDetail(
            @PathVariable Long caseId,
            @PathVariable Long evidenceId
    ) {
        return ResponseEntity.ok(evidenceService.getEvidenceDetail(caseId, evidenceId));
    }

    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<Void> deleteEvidence(
            @PathVariable Long caseId,
            @PathVariable Long evidenceId
    ) {
        evidenceService.deleteEvidence(caseId, evidenceId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}