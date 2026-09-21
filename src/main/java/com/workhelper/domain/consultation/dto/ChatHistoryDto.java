package com.workhelper.domain.consultation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 상담 대화 이력 DTO
 *
 * AI에게 지금까지의 상담 대화 내용을 전달할 때 사용한다.
 *
 * 예시:
 * {
 *   "role": "USER",
 *   "content": "월급 일부를 못 받았어요."
 * }
 */
@Getter
@NoArgsConstructor
public class ChatHistoryDto {

    /**
     * 메시지를 보낸 주체
     *
     * 예:
     * USER      → 사용자
     * ASSISTANT → AI 상담사
     */
    private String role;

    /**
     * 실제 상담 메시지 내용
     */
    private String content;
}