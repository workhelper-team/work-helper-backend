package com.workhelper.domain.consultation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhelper.domain.consultation.dto.ConsultationMessageRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationMessageResponseDto;
import com.workhelper.domain.consultation.entity.ConsultationMessage;
import com.workhelper.domain.consultation.entity.MessageRole;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import com.workhelper.infra.ai.AiClient;
import com.workhelper.infra.ai.AiIntegrationException;
import com.workhelper.infra.ai.dto.ConsultationAiRequest;
import com.workhelper.infra.ai.dto.ConsultationAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsultationMessageServiceTest {

    private final ConsultationMessageRepository messageRepository = mock(ConsultationMessageRepository.class);
    private final LaborCaseRepository laborCaseRepository = mock(LaborCaseRepository.class);
    private final AiClient aiClient = mock(AiClient.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private ConsultationMessageService service;
    private LaborCase laborCase;

    @BeforeEach
    void setUp() {
        laborCase = LaborCase.builder().userId(7L).build();
        ReflectionTestUtils.setField(laborCase, "caseId", 3L);
        when(laborCaseRepository.findByCaseIdAndUserId(3L, 7L)).thenReturn(Optional.of(laborCase));
        when(messageRepository.save(any(ConsultationMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        service = new ConsultationMessageService(messageRepository, laborCaseRepository,
                aiClient, new ObjectMapper().findAndRegisterModules(), transactionManager);
    }

    @Test
    void sendsOnlyEarlierMessagesAndSavesUserAndAssistant() {
        when(messageRepository.findByLaborCase_CaseIdOrderByCreatedAtAsc(3L)).thenReturn(List.of(
                message(MessageRole.USER, "earlier question"),
                message(MessageRole.ASSISTANT, "earlier answer")));
        ConsultationAiResponse.Precedent precedent = new ConsultationAiResponse.Precedent(
                "2026-1", "case", "court", "2026-09-18", "decision", "precedent text");
        when(aiClient.consult(any())).thenReturn(new ConsultationAiResponse("new answer", List.of(precedent)));

        ConsultationMessageResponseDto response = service.sendMessage(3L, 7L, request("new question"));

        ArgumentCaptor<ConsultationAiRequest> aiRequest = ArgumentCaptor.forClass(ConsultationAiRequest.class);
        verify(aiClient).consult(aiRequest.capture());
        assertThat(aiRequest.getValue().question()).isEqualTo("new question");
        assertThat(aiRequest.getValue().chatHistory()).containsExactly(
                new ConsultationAiRequest.ChatMessage(ConsultationAiRequest.Role.USER, "earlier question"),
                new ConsultationAiRequest.ChatMessage(ConsultationAiRequest.Role.ASSISTANT, "earlier answer"));

        ArgumentCaptor<ConsultationMessage> savedMessages = ArgumentCaptor.forClass(ConsultationMessage.class);
        verify(messageRepository, times(2)).save(savedMessages.capture());
        ConsultationMessage user = savedMessages.getAllValues().get(0);
        ConsultationMessage assistant = savedMessages.getAllValues().get(1);
        assertThat(user.getRole()).isEqualTo(MessageRole.USER);
        assertThat(user.getContent()).isEqualTo("new question");
        assertThat(user.getStructuredResult()).isNull();
        assertThat(assistant.getRole()).isEqualTo(MessageRole.ASSISTANT);
        assertThat(assistant.getContent()).isEqualTo("new answer");
        assertThat(assistant.getStructuredResult().path("precedents").get(0).path("case_number").asText())
                .isEqualTo("2026-1");
        assertThat(assistant.getStructuredResult().path("answer").asText()).isEqualTo("new answer");
        assertThat(assistant.getStructuredResult().path("precedents").get(0).path("caseNumber").isMissingNode())
                .isTrue();
        assertThat(response.getRole()).isEqualTo("USER");

        InOrder order = inOrder(messageRepository, aiClient);
        order.verify(messageRepository).findByLaborCase_CaseIdOrderByCreatedAtAsc(3L);
        order.verify(messageRepository).save(argThat(saved -> saved.getRole() == MessageRole.USER));
        order.verify(aiClient).consult(any());
        order.verify(messageRepository).save(argThat(saved -> saved.getRole() == MessageRole.ASSISTANT));
        verify(transactionManager, times(2)).commit(any());
    }

    @Test
    void aiFailureLeavesOnlyCommittedUserMessage() {
        when(messageRepository.findByLaborCase_CaseIdOrderByCreatedAtAsc(3L)).thenReturn(List.of());
        when(aiClient.consult(any())).thenThrow(new AiIntegrationException(
                AiIntegrationException.Kind.TIMEOUT, null, "AI server request timed out", null));

        assertThatThrownBy(() -> service.sendMessage(3L, 7L, request("new question")))
                .isInstanceOf(AiIntegrationException.class);

        ArgumentCaptor<ConsultationMessage> savedMessage = ArgumentCaptor.forClass(ConsultationMessage.class);
        verify(messageRepository).save(savedMessage.capture());
        assertThat(savedMessage.getValue().getRole()).isEqualTo(MessageRole.USER);
        verify(transactionManager).commit(any());
    }

    private ConsultationMessage message(MessageRole role, String content) {
        return ConsultationMessage.builder()
                .laborCase(laborCase)
                .role(role)
                .content(content)
                .build();
    }

    private ConsultationMessageRequestDto request(String content) {
        ConsultationMessageRequestDto request = new ConsultationMessageRequestDto();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }
}
