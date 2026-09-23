
package com.workhelper.domain.evidence.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.OffsetDateTime;

public record EvidenceDetailResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String description,
        String fileUrl,
        String extractedText,
        JsonNode analysisResult,
        String analysisStatus,
        OffsetDateTime createdAt
) {
}