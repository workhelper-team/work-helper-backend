package com.workhelper.domain.expert.dto;

/**
 * 질문 등록(F-QA-001) 요청입니다.
 * Controller가 JSON 요청 본문을 이 record로 변환해 Service에 전달합니다.
 */
public record ExpertQuestionRequest(
        // 질문 제목입니다.
        String title,
        // 질문 본문입니다.
        String content
) {
}