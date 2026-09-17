// AI 분석 클라이언트 인터페이스 생성
package com.workhelper.domain.evidence.client;

public interface AiEvidenceAnalysisClient {

    /**
     * FastAPI(AI Backend)에 분석을 요청하고 결과를 받아온다.
     * 실패 시 예외를 던진다 (호출 측에서 FAILED 상태 처리).
     */
    AiAnalysisResult analyze(AiAnalysisRequest request);
}