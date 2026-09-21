package com.workhelper.domain.expert.dto;

import java.time.OffsetDateTime;

/** 질문 목록에서 한 건을 간단히 표현하는 응답입니다. */
public record ExpertQuestionSummaryResponse(
        // 질문 ID입니다.
        Long questionId,
        // 사건 ID입니다.
        Long caseId,
        // 질문 제목입니다.
        String title,
        // 질문 상태입니다.
        String status,
        // 질문 생성 시간입니다.
        OffsetDateTime createdAt
) {
}