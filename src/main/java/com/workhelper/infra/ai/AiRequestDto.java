package com.workhelper.infra.ai;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * FastAPI AI 서버 요청 DTO
 *
 * @param taskType 작업 유형 (OCR / RAG_QUERY / PETITION_GENERATION)
 * @param payload  실제 입력 데이터 (텍스트, 파일 경로, 사실관계 등)
 * @param options  모델/언어 등 추가 옵션
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRequestDto(
        String taskType,
        String payload,
        Map<String, Object> options
) {
    public static AiRequestDto of(String taskType, String payload) {
        return new AiRequestDto(taskType, payload, null);
    }

    public static AiRequestDto of(String taskType, String payload, Map<String, Object> options) {
        return new AiRequestDto(taskType, payload, options);
    }
}
