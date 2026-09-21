package com.workhelper.domain.consultation.service;

import com.workhelper.domain.consultation.dto.ConsultationMessageRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationMessageResponseDto;
import com.workhelper.domain.consultation.entity.ConsultationMessage;
import com.workhelper.domain.consultation.entity.MessageRole;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationMessageService {

    private final ConsultationMessageRepository consultationMessageRepository;
    private final LaborCaseRepository laborCaseRepository;

    public List<ConsultationMessageResponseDto> getMessages(Long caseId) {

        List<ConsultationMessage> messages =
                consultationMessageRepository
                        .findByLaborCase_IdOrderByCreatedAtAsc(caseId);

        return messages.stream()
                .map(ConsultationMessageResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultationMessageResponseDto sendMessage(
            Long caseId,
            ConsultationMessageRequestDto requestDto
    ) {

        LaborCase laborCase = laborCaseRepository.findById(caseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + caseId
                        )
                );

        ConsultationMessage userMessage =
                ConsultationMessage.builder()
                        .laborCase(laborCase)
                        .role(MessageRole.USER)
                        .content(requestDto.getContent())
                        .structuredResult(null)
                        .build();

        ConsultationMessage savedMessage =
                consultationMessageRepository.save(userMessage);

        return new ConsultationMessageResponseDto(savedMessage);
    }
}