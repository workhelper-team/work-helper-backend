package com.workhelper.infra.ai;

import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * FastAPI AI 서버 통신 클라이언트
 * - OCR, RAG 질의, 진정서 생성 요청/응답 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final WebClient aiWebClient;

    /**
     * OCR: 이미지/PDF 문서에서 텍스트 추출
     *
     * @param filePath AI 서버가 접근 가능한 파일 경로(또는 식별자)
     */
    public AiResponseDto extractTextByOcr(String filePath) {
        AiRequestDto request = AiRequestDto.of("OCR", filePath);
        return post("/api/v1/ai/ocr", request);
    }

    /**
     * RAG 질의: 법률 지식 기반 질의응답
     */
    public AiResponseDto ragQuery(String question) {
        AiRequestDto request = AiRequestDto.of("RAG_QUERY", question);
        return post("/api/v1/ai/rag", request);
    }

    /**
     * 진정서 생성: 사실관계 텍스트 기반 진정서 초안 생성
     */
    public AiResponseDto generatePetition(String facts) {
        AiRequestDto request = AiRequestDto.of("PETITION_GENERATION", facts);
        return post("/api/v1/ai/petition", request);
    }

    /**
     * 옵션 포함 범용 호출
     */
    public AiResponseDto request(String taskType, String payload, Map<String, Object> options) {
        return post("/api/v1/ai/request", AiRequestDto.of(taskType, payload, options));
    }

    private AiResponseDto post(String uri, AiRequestDto request) {
        try {
            return aiWebClient.post()
                    .uri(uri)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiResponseDto.class)
                    .timeout(TIMEOUT)
                    .onErrorResume(WebClientException.class, e -> {
                        log.error("AI server call failed: uri={}, error={}", uri, e.getMessage());
                        return Mono.error(new BusinessException(ErrorCode.AI_SERVER_ERROR));
                    })
                    .block();
        } catch (WebClientException e) {
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }
    }
}
