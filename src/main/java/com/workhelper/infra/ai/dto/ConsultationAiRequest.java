package com.workhelper.infra.ai.dto;

import java.util.List;

public record ConsultationAiRequest(List<ChatMessage> chatHistory, String question) {

    public record ChatMessage(Role role, String content) {
    }

    public enum Role {
        USER,
        ASSISTANT
    }
}
