package com.workhelper.domain.evidence.dto;

import java.time.OffsetDateTime;

public record EvidenceSummaryResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String description,
        String analysisStatus,
        OffsetDateTime createdAt
) {
}