package com.workhelper.domain.evidence.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record EvidenceAnalysisResponse(
        Long evidenceId,
        String extractedText,
        JsonNode analysisResult,
        String analysisStatus
) {
}