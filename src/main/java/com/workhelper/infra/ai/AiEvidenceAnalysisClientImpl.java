package com.workhelper.infra.ai;

import com.workhelper.domain.evidence.client.AiAnalysisRequest;
import com.workhelper.domain.evidence.client.AiAnalysisResult;
import com.workhelper.domain.evidence.client.AiEvidenceAnalysisClient;
import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Spring Boot에서 AI 분석 서버로 요청을 전달하는 인프라 어댑터.
 *
 * base-url과 내부 경로, 요청/응답 DTO는 AI팀과 API 계약이 확정되면 함께 고정해야 한다.
 * 현재 storagePath를 전달하므로 AI 서버가 애플리케이션 로컬 파일에 접근할 수 없는
 * 배포 환경에서는 S3 object key 또는 접근 가능한 presigned URL 전달 방식으로 바꿔야 한다.
 */
@Component
public class AiEvidenceAnalysisClientImpl implements AiEvidenceAnalysisClient {

    private final RestClient restClient;

    public AiEvidenceAnalysisClientImpl(@Value("${ai.analysis.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public AiAnalysisResult analyze(AiAnalysisRequest request) {
        return restClient.post()
                .uri("/internal/evidences/analyze")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new BusinessException(
                            ErrorCode.UPSTREAM_ERROR,
                            "AI 분석 서비스 호출에 실패했습니다. (Status Code: " + resp.getStatusCode() + ")"
                    );
                })
                .body(AiAnalysisResult.class);
    }
}
