package com.workhelper.domain.expert.dto;

/**
 * 답변 등록(F-QA-005) 요청입니다.
 * Controller가 JSON 요청 본문을 이 record로 변환해 Service에 전달합니다.
 */
public record ExpertAnswerRequest(
        // 전문가가 작성할 답변 본문입니다.
        String content
) {
}