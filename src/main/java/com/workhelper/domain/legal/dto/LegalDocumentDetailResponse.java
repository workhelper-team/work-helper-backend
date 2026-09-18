package com.workhelper.domain.legal.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record LegalDocumentDetailResponse(
        Long legalDocumentId,
        String title,
        String fullText,
        String sourceType,
        String sourceUrl,
        JsonNode metadata
) {
}