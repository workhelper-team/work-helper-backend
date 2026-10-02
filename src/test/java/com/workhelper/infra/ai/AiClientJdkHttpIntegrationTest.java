package com.workhelper.infra.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import com.workhelper.infra.ai.dto.ConsultationAiRequest;
import com.workhelper.infra.ai.dto.ConsultationAiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AiClientJdkHttpIntegrationTest {

    @Test
    void consultationSendsJsonBodyThroughJdkHttpClient() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        AtomicReference<ReceivedRequest> received = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal/ai/consultation", exchange -> {
            byte[] body = exchange.getRequestBody().readAllBytes();
            received.set(new ReceivedRequest(exchange.getRequestMethod(), exchange.getProtocol(),
                    exchange.getRequestHeaders().getFirst("Content-Type"),
                    exchange.getRequestHeaders().getFirst("Upgrade"), body));
            byte[] response = "{\"answer\":\"정상 응답\",\"precedents\":[]}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();

        try {
            String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
            AiClient client = new AiClient(new AiClientConfig().aiRestClient(
                    baseUrl, Duration.ofSeconds(5), Duration.ofSeconds(5)), objectMapper);
            ConsultationAiResponse response = client.consult(new ConsultationAiRequest(
                    List.of(new ConsultationAiRequest.ChatMessage(
                            ConsultationAiRequest.Role.USER, "테스트 상담입니다.")),
                    "테스트 상담입니다."));

            assertThat(response.answer()).isEqualTo("정상 응답");
            assertThat(received.get()).isNotNull();
            assertThat(received.get().method()).isEqualTo("POST");
            assertThat(received.get().protocol()).isEqualTo("HTTP/1.1");
            assertThat(received.get().upgrade()).isNull();
            assertThat(MediaType.APPLICATION_JSON.isCompatibleWith(
                    MediaType.parseMediaType(received.get().contentType()))).isTrue();
            byte[] expectedBody = """
                    {"chatHistory":[{"role":"USER","content":"테스트 상담입니다."}],"question":"테스트 상담입니다."}
                    """.trim().getBytes(StandardCharsets.UTF_8);
            assertThat(received.get().body()).containsExactly(expectedBody);
            assertThat(received.get().body().length).isEqualTo(expectedBody.length).isGreaterThan(0);
            JsonNode json = objectMapper.readTree(received.get().body());
            assertThat(json.path("chatHistory").get(0).path("role").asText()).isEqualTo("USER");
            assertThat(json.path("chatHistory").get(0).path("content").asText())
                    .isEqualTo("테스트 상담입니다.");
            assertThat(json.path("question").asText()).isEqualTo("테스트 상담입니다.");
            assertThat(json.size()).isEqualTo(2);
        } finally {
            server.stop(0);
        }
    }

    private record ReceivedRequest(String method, String protocol, String contentType,
                                   String upgrade, byte[] body) {
    }
}
