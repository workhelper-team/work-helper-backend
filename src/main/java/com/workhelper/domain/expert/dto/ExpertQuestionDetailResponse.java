package com.workhelper.domain.expert.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** 질문 하나의 상세 정보와 그 질문에 달린 답변 목록을 함께 담는 응답입니다. */
public record ExpertQuestionDetailResponse(
        // 질문 ID입니다.
        Long questionId,
        // 사건 ID입니다.
        Long caseId,
        // 질문 제목입니다.
        String title,
        // 질문 본문입니다.
        String content,
        // 질문 상태입니다.
        String status,
        // 질문 생성 시간입니다.
        OffsetDateTime createdAt,
        // 질문에 등록된 답변 목록입니다.
        List<ExpertAnswerResponse> answers
) {
}