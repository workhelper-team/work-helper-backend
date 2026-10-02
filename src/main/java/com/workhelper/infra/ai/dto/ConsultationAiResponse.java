package com.workhelper.infra.ai.dto;

import java.util.List;

public record ConsultationAiResponse(String answer, List<Precedent> precedents) {

    public record Precedent(
            String caseNumber,
            String caseName,
            String courtName,
            String judgmentDate,
            String judgmentType,
            String content) {
    }
}
