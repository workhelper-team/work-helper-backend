package com.workhelper.domain.expert.dto;

import java.time.OffsetDateTime;

/** 답변 등록 결과 또는 질문 상세 응답의 답변 한 건을 표현합니다. */
public record ExpertAnswerResponse(
        // 답변 ID입니다.
        Long answerId,
        // 답변이 달린 질문의 ID입니다.
        Long questionId,
        // 답변을 작성한 전문가의 ID입니다.
        Long expertId,
        // 답변 본문입니다.
        String content,
        // 답변 생성 시간입니다.
        OffsetDateTime createdAt
) {
}