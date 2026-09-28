package com.workhelper.infra.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.infra.ai.dto.ConsultationAiRequest;
import com.workhelper.infra.ai.dto.ConsultationAiResponse;
import com.workhelper.infra.ai.dto.DocumentDraftAiRequest;
import com.workhelper.infra.ai.dto.DocumentDraftAiResponse;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiRequest;
import com.workhelper.infra.ai.dto.EvidenceAnalysisAiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void consultationUsesCurrentContractAndReadsSnakeCasePrecedent() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiClient client = new AiClient(builder.build(), objectMapper);
        server.expect(requestTo("http://localhost:8000/internal/ai/consultation"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"chatHistory":[{"role":"ASSISTANT","content":"prior answer"}],"question":"new question"}
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {"consultationResult":{"answer":"answer","precedents":[{"case_number":"2026-1",
                        "case_name":"case","court_name":"court","judgment_date":"2026-09-18",
                        "judgment_type":"type","content":"text"}]}}
                        """, MediaType.APPLICATION_JSON));

        ConsultationAiResponse response = client.consult(new ConsultationAiRequest(
                List.of(new ConsultationAiRequest.ChatMessage(ConsultationAiRequest.Role.ASSISTANT, "prior answer")),
                "new question"));

        assertThat(response.consultationResult().answer()).isEqualTo("answer");
        assertThat(response.consultationResult().precedents().get(0).caseNumber()).isEqualTo("2026-1");
        server.verify();
    }

    @Test
    void evidenceAnalysisUsesFileUrlAndUserContext() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiClient client = new AiClient(builder.build(), objectMapper);
        server.expect(requestTo("http://localhost:8000/internal/ai/evidence-analysis"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"fileUrl":"https://example.com/file","userContext":"context"}
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {"extractedText":"ocr","analysisSummary":"summary"}
                        """, MediaType.APPLICATION_JSON));

        EvidenceAnalysisAiResponse response = client.analyzeEvidence(
                new EvidenceAnalysisAiRequest("https://example.com/file", "context"));

        assertThat(response.extractedText()).isEqualTo("ocr");
        assertThat(response.analysisSummary()).isEqualTo("summary");
        server.verify();
    }

    @Test
    void documentDraftUsesLowercaseRolesAndNullableEvidence() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiClient client = new AiClient(builder.build(), objectMapper);
        server.expect(requestTo("http://localhost:8000/internal/ai/document-draft"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"caseId":1,"chatHistory":[{"role":"user","content":"question"}],
                        "evidenceDocument":null}
                        """, JsonCompareMode.STRICT))
                .andRespond(withSuccess("""
                        {"success":true,"caseId":1,
                        "complainant":{"name":null,"birthDate":"2026-09-18","address":null,
                        "phone":null,"mobilePhone":null,"email":null,"receiveStatus":null},
                        "respondent":null,"facts":null,
                        "content":{"claimReason":"reason","targetLaborOffice":null,"totalUnpaidAmount":0}}
                        """, MediaType.APPLICATION_JSON));

        DocumentDraftAiResponse response = client.draftDocument(new DocumentDraftAiRequest(
                1L, List.of(new DocumentDraftAiRequest.ChatMessage(DocumentDraftAiRequest.Role.USER, "question")),
                null));

        assertThat(response.complainant().birthDate()).isEqualTo(LocalDate.of(2026, 9, 18));
        assertThat(response.respondent()).isNull();
        server.verify();
    }

    @Test
    void fastApiDetailIsPreservedForValidationError() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiClient client = new AiClient(builder.build(), objectMapper);
        server.expect(requestTo("http://localhost:8000/internal/ai/evidence-analysis"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\":\"invalid fileUrl\"}"));

        assertThatThrownBy(() -> client.analyzeEvidence(new EvidenceAnalysisAiRequest("bad", null)))
                .isInstanceOfSatisfying(AiIntegrationException.class, exception -> {
                    assertThat(exception.getKind()).isEqualTo(AiIntegrationException.Kind.BAD_REQUEST);
                    assertThat(exception.getStatusCode()).isEqualTo(422);
                    assertThat(exception.getMessage()).contains("invalid fileUrl");
                });
        server.verify();
    }

    @Test
    void serverErrorIsClassifiedSeparately() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:8000");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiClient client = new AiClient(builder.build(), objectMapper);
        server.expect(requestTo("http://localhost:8000/internal/ai/consultation"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"detail\":\"LLM failed\"}"));

        assertThatThrownBy(() -> client.consult(new ConsultationAiRequest(List.of(), "question")))
                .isInstanceOfSatisfying(AiIntegrationException.class, exception -> {
                    assertThat(exception.getKind()).isEqualTo(AiIntegrationException.Kind.SERVER_ERROR);
                    assertThat(exception.getStatusCode()).isEqualTo(500);
                });
        server.verify();
    }

    @Test
    void transportFailuresDistinguishTimeoutFromConnectionFailure() {
        RestClient timeoutClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .requestFactory((uri, method) -> {
                    throw new SocketTimeoutException("read timed out");
                })
                .build();
        RestClient connectionClient = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .requestFactory((uri, method) -> {
                    throw new ConnectException("connection refused");
                })
                .build();

        assertThatThrownBy(() -> new AiClient(timeoutClient, objectMapper)
                .consult(new ConsultationAiRequest(List.of(), "question")))
                .isInstanceOfSatisfying(AiIntegrationException.class, exception ->
                        assertThat(exception.getKind()).isEqualTo(AiIntegrationException.Kind.TIMEOUT));
        assertThatThrownBy(() -> new AiClient(connectionClient, objectMapper)
                .consult(new ConsultationAiRequest(List.of(), "question")))
                .isInstanceOfSatisfying(AiIntegrationException.class, exception ->
                        assertThat(exception.getKind()).isEqualTo(AiIntegrationException.Kind.CONNECTION_FAILURE));
    }
}
