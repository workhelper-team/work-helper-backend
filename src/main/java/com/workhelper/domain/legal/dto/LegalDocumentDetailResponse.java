package com.workhelper.domain.legal.dto;

/**
 * API-LEGAL-002 응답
 * 명세: 제목, 원문, 출처정보
 */
public record LegalDocumentDetailResponse(
        Long legalDocumentId,
        String title,
        String fullText,
        String sourceType,
        String sourceId,
        String sourceUrl
) {
}
