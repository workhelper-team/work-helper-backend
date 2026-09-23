package com.workhelper.domain.consultation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 상담 AI 요청 DTO
 *
 * AI 상담 API에 전달할 전체 요청 데이터를 담는다.
 *
 * 요청 예시:
 * {
 *   "caseContext": "임금 체불 관련 사건입니다.",
 *   "chatHistory": [
 *     {
 *       "role": "USER",
 *       "content": "월급을 받지 못했습니다."
 *     }
 *   ],
 *   "question": "이 경우 어떻게 해야 하나요?"
 * }
 */
@Getter
@NoArgsConstructor
public class ConsultationAiRequestDto {

    /**
     * 현재 상담 사건의 전체적인 맥락
     *
     * 예:
     * "사용자가 퇴사했지만 마지막 월급 일부를 받지 못한 사건"
     */
    private String caseContext;

    /**
     * 지금까지 진행된 상담 대화 목록
     *
     * 여러 개의 ChatHistoryDto를 담는다.
     */
    private List<ChatHistoryDto> chatHistory;

    /**
     * 사용자가 현재 AI에게 질문한 내용
     *
     * 예:
     * "이 경우 노동청에 신고할 수 있나요?"
     */
    private String question;
}