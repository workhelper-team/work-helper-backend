////증거 분석 응답 DTO 
package com.workhelper.domain.evidence.dto;

/**
 * API-EVD-002 응답
 * 명세: extractedText, analysisResult, analysisStatus
 * (분석 재시도 시에도 동일 응답 형태 사용)
 */
public record EvidenceAnalysisResponse(
        Long evidenceId,
        String extractedText,
        String analysisResult,
        String analysisStatus
) {
}