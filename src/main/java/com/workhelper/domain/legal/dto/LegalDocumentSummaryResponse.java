package com.workhelper.domain.legal.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** API-LEGAL-001 검색 결과 항목 */
public record LegalDocumentSummaryResponse(
        Long legalDocumentId,
        String title,
        String snippet,
        JsonNode metadata,
        String sourceType,
        String sourceUrl
) {
}