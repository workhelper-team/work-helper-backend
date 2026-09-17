//증거목록조회 응답 DTO

package com.workhelper.domain.evidence.dto;

import java.time.OffsetDateTime;

/**
 * API-EVD-003 응답 (목록의 항목 하나)
 * 명세: 증거 목록 및 분석 상태
 */
public record EvidenceSummaryResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String analysisStatus,
        OffsetDateTime createdAt
) {
}