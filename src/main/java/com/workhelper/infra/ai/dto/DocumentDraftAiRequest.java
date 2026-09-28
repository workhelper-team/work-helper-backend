package com.workhelper.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DocumentDraftAiRequest(
        Long caseId,
        List<ChatMessage> chatHistory,
        EvidenceDocument evidenceDocument) {

    public record ChatMessage(Role role, String content) {
    }

    public enum Role {
        @JsonProperty("user") USER,
        @JsonProperty("assistant") ASSISTANT
    }

    public record EvidenceDocument(String extractedText, String analysisSummary) {
    }
}
