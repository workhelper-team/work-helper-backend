// AI 분석결과 DTO
package com.workhelper.domain.evidence.client;

/**
 * FastAPI(AI Backend)로부터 받은 분석 결과.
 */
public record AiAnalysisResult(
        String extractedText,
        String analysisResultJson
) {
}