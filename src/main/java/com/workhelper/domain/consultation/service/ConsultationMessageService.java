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

    @Transactional
    public ConsultationMessageResponseDto sendMessage(
            Long caseId,
            Long userId,
            ConsultationMessageRequestDto requestDto
    ) {
        // 현재 로그인한 사용자가 해당 사건의 소유자인지 확인
        LaborCase laborCase = laborCaseRepository
                .findByCaseIdAndUserId(caseId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건에 접근할 권한이 없습니다. ID: " + caseId
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