package com.workhelper.domain.consultation.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConsultationMessageService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final LaborCaseRepository laborCaseRepository;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public ConsultationMessageService(
            ConsultationMessageRepository consultationMessageRepository,
            LaborCaseRepository laborCaseRepository,
            AiClient aiClient,
            ObjectMapper objectMapper,
            PlatformTransactionManager transactionManager) {
        this.consultationMessageRepository = consultationMessageRepository;
        this.laborCaseRepository = laborCaseRepository;
        this.aiClient = aiClient;
        this.objectMapper = objectMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Transactional(readOnly = true)
    public List<ConsultationMessageResponseDto> getMessages(
            Long caseId,
            Long userId
    ) {
        // 현재 로그인한 사용자가 해당 사건의 소유자인지 확인
        LaborCase laborCase = laborCaseRepository
                .findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건에 접근할 권한이 없습니다. ID: " + caseId
                        )
                );

        List<ConsultationMessage> messages =
                consultationMessageRepository
                        .findByLaborCase_CaseIdOrderByCreatedAtAsc(
                                laborCase.getCaseId()
                        );

        return messages.stream()
                .map(ConsultationMessageResponseDto::new)
                .collect(Collectors.toList());
    }

    public ConsultationMessageResponseDto sendMessage(
            Long caseId,
            Long userId,
            ConsultationMessageRequestDto requestDto
    ) {
        // 이전 대화만 조회한 다음 현재 질문을 저장하고, AI 호출 전에 커밋합니다.
        SendContext context = transactionTemplate.execute(status -> {
            LaborCase laborCase = laborCaseRepository
                    .findByCaseIdAndUserId(caseId, userId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 사건에 접근할 권한이 없습니다. ID: " + caseId));

            List<ConsultationAiRequest.ChatMessage> chatHistory = consultationMessageRepository
                    .findByLaborCase_CaseIdOrderByCreatedAtAsc(laborCase.getCaseId())
                    .stream()
                    .map(message -> new ConsultationAiRequest.ChatMessage(
                            ConsultationAiRequest.Role.valueOf(message.getRole().name()),
                            message.getContent()))
                    .toList();

            ConsultationMessage userMessage = ConsultationMessage.builder()
                    .laborCase(laborCase)
                    .role(MessageRole.USER)
                    .content(requestDto.getContent())
                    .structuredResult(null)
                    .build();
            ConsultationMessage savedMessage = consultationMessageRepository.save(userMessage);
            return new SendContext(laborCase, chatHistory,
                    new ConsultationMessageResponseDto(savedMessage));
        });

        ConsultationAiResponse aiResponse = aiClient.consult(
                new ConsultationAiRequest(context.chatHistory(), requestDto.getContent()));
        if (aiResponse.consultationResult() == null
                || !StringUtils.hasText(aiResponse.consultationResult().answer())) {
            throw new AiIntegrationException(AiIntegrationException.Kind.INVALID_RESPONSE, null,
                    "AI server returned no consultation answer", null);
        }

        JsonNode structuredResult = objectMapper.valueToTree(aiResponse.consultationResult());
        transactionTemplate.executeWithoutResult(status -> consultationMessageRepository.save(
                ConsultationMessage.builder()
                        .laborCase(context.laborCase())
                        .role(MessageRole.ASSISTANT)
                        .content(aiResponse.consultationResult().answer())
                        .structuredResult(structuredResult)
                        .build()));

        return context.userResponse();
    }

    private record SendContext(
            LaborCase laborCase,
            List<ConsultationAiRequest.ChatMessage> chatHistory,
            ConsultationMessageResponseDto userResponse) {
    }
}
