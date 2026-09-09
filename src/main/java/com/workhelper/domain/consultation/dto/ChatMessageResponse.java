package com.workhelper.domain.consultation.dto;

import java.util.List;

/**
 * 챗봇 메시지 응답 DTO
 *
 * @param references 참조 법령/판례 목록 (RAG 검색 근거)
 */
public record ChatMessageResponse(
        String sessionId,
        String answer,
        List<String> references
) {
}
