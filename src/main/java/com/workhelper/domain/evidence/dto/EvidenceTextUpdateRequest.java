package com.workhelper.domain.evidence.dto;

import jakarta.validation.constraints.NotNull;

public record EvidenceTextUpdateRequest(@NotNull String extractedText) {
}
