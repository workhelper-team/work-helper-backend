package com.workhelper.infra.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.infra.ai.dto.ConsultationAiRequest;
import com.workhelper.infra.ai.dto.ConsultationAiResponse;
import com.workhelper.infra.ai.dto.DocumentDraftAiRequest;
import com.workhelper.infra.ai.dto.DocumentDraftAiResponse;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiRequest;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;

@Component
public class AiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiClient(@Qualifier("aiRestClient") RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public ConsultationAiResponse consult(ConsultationAiRequest request) {
        return post("/internal/ai/consultation", request, ConsultationAiResponse.class);
    }

    public EvidenceAnalysisAiResponse analyzeEvidence(EvidenceAnalysisAiRequest request) {
        return post("/internal/ai/evidence-analysis", request, EvidenceAnalysisAiResponse.class);
    }

    public DocumentDraftAiResponse draftDocument(DocumentDraftAiRequest request) {
        return post("/internal/ai/document-draft", request, DocumentDraftAiResponse.class);
    }

    private <T> T post(String path, Object request, Class<T> responseType) {
        try {
            T response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(responseType);
            if (response == null) {
                throw new AiIntegrationException(
                        AiIntegrationException.Kind.INVALID_RESPONSE, null,
                        "AI server returned an empty response", null);
            }
            return response;
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            AiIntegrationException.Kind kind = status == 400 || status == 422
                    ? AiIntegrationException.Kind.BAD_REQUEST
                    : status >= 500 ? AiIntegrationException.Kind.SERVER_ERROR
                    : AiIntegrationException.Kind.HTTP_ERROR;
            throw new AiIntegrationException(kind, status,
                    "AI server returned HTTP " + status + ": " + errorDetail(exception), exception);
        } catch (ResourceAccessException exception) {
            AiIntegrationException.Kind kind = isTimeout(exception)
                    ? AiIntegrationException.Kind.TIMEOUT
                    : AiIntegrationException.Kind.CONNECTION_FAILURE;
            throw new AiIntegrationException(kind, null,
                    kind == AiIntegrationException.Kind.TIMEOUT
                            ? "AI server request timed out" : "Could not connect to AI server",
                    exception);
        } catch (RestClientException exception) {
            throw new AiIntegrationException(
                    AiIntegrationException.Kind.INVALID_RESPONSE, null,
                    "Could not read AI server response", exception);
        }
    }

    private String errorDetail(RestClientResponseException exception) {
        String body = exception.getResponseBodyAsString();
        if (body.isBlank()) {
            return exception.getStatusText();
        }
        try {
            JsonNode detail = objectMapper.readTree(body).path("detail");
            if (detail.isTextual()) {
                return detail.asText();
            }
            if (!detail.isMissingNode() && !detail.isNull()) {
                return detail.toString();
            }
        } catch (JsonProcessingException ignored) {
            // A non-JSON error body has no FastAPI detail field.
        }
        return exception.getStatusText();
    }

    private boolean isTimeout(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SocketTimeoutException || cause instanceof HttpTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
