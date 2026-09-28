package com.workhelper.domain.document.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DocumentUpdateRequest(
        @Size(max = 200) String title,
        @NotNull JsonNode complainant,
        @NotNull JsonNode respondent,
        @NotNull JsonNode facts,
        @NotNull @Valid Content content) {
    public record Content(@NotNull String claimReason, String targetLaborOffice,
                          BigDecimal totalUnpaidAmount) {
    }
}
