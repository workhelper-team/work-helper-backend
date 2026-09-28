package com.workhelper.domain.document.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DocumentDetailResponse(
        Long documentId, Long caseId, String documentType, String title,
        JsonNode complainant, JsonNode respondent, JsonNode facts, Content content,
        OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    public record Content(String claimReason, String targetLaborOffice,
                          BigDecimal totalUnpaidAmount) {
    }
}
