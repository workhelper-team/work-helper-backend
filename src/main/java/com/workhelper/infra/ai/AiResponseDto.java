package com.workhelper.infra.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * FastAPI AI 서버 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiResponseDto(
        boolean success,
        String taskType,
        String result,
        Map<String, Object> metadata
) {
}
