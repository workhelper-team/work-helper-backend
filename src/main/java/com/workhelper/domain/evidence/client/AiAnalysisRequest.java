//AI 분석 요청 DTO

package com.workhelper.domain.evidence.client;

/**
 * Spring Boot -> FastAPI(AI Backend) 내부 호출용 요청 모델.
 * 실제 필드명/경로는 AI팀과 계약 확정 후 조정 필요.
 */
public record AiAnalysisRequest(
        Long evidenceId,
        String storagePath,
        String mimeType
) {
}