package com.workhelper.domain.consultation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 챗봇 메시지 요청 DTO
 */
public record ChatMessageRequest(

        @NotBlank(message = "세션 ID는 필수입니다.")
        String sessionId,

        @NotBlank(message = "질문 내용은 필수입니다.")
        String message
) {
}
