package com.workhelper.domain.expert.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

/** 질문 목록에서 한 건을 간단히 표현하는 응답입니다. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpertQuestionSummaryResponse(
        // 질문 ID입니다.
        Long questionId,
        // 사건 ID입니다.
        Long caseId,
        // 질문 제목입니다.
        String title,
        // 질문 상태입니다.
        String status,
        // 연결된 사건의 카테고리입니다. 전문가 목록에서만 사용합니다.
        String category,
        // 질문에 등록된 답변 수입니다.
        long answerCount,
        // 질문 생성 시간입니다.
        OffsetDateTime createdAt
) {
}