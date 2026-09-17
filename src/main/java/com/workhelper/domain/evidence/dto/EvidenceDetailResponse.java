// 증거 상세 조회 응답 DTO
package com.workhelper.domain.evidence.dto;

import java.time.OffsetDateTime;

/**
 * API-EVD-004 응답
 * 명세: 파일정보, 추출 텍스트, 분석 결과
 */
public record EvidenceDetailResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String extractedText,
        String analysisResult,
        String analysisStatus,
        OffsetDateTime createdAt
) {
} 