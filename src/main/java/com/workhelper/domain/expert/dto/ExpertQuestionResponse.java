package com.workhelper.domain.expert.dto;

import java.time.OffsetDateTime;

/** 질문 등록이 성공했을 때 클라이언트에 돌려주는 응답입니다. */
public record ExpertQuestionResponse(
        // 생성된 질문의 ID입니다.
        Long questionId,
        // 질문이 연결된 사건의 ID입니다.
        Long caseId,
        // 질문 제목입니다.
        String title,
        // 질문 본문입니다.
        String content,
        // WAITING 또는 ANSWERED 상태입니다.
        String status,
        // 질문 생성 시간입니다.
        OffsetDateTime createdAt
) {
}