package com.workhelper.domain.legal.dto;

/**
 * API-LEGAL-001 응답 (목록의 항목 하나)
 * 명세: 법률자료 검색 결과
 * snippet은 명세에 명시된 필드는 아니지만, 검색 결과 UX를 위해 본문 일부를 잘라서 함께 제공 (필요 없으면 제거 가능)
 */
public record LegalDocumentSummaryResponse(
        Long legalDocumentId,
        String title,
        String sourceType,
        String snippet,
        String sourceUrl
) {
}