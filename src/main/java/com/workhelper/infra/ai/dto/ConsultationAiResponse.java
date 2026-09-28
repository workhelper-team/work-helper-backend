package com.workhelper.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ConsultationAiResponse(ConsultationResult consultationResult) {

    public record ConsultationResult(String answer, List<Precedent> precedents) {
    }

    public record Precedent(
            @JsonProperty("case_number") String caseNumber,
            @JsonProperty("case_name") String caseName,
            @JsonProperty("court_name") String courtName,
            @JsonProperty("judgment_date") String judgmentDate,
            @JsonProperty("judgment_type") String judgmentType,
            String content) {
    }
}
