package com.workhelper.domain.consultation.service;

import com.workhelper.domain.consultation.dto.ConsultationRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationResponseDto;
import com.workhelper.domain.consultation.entity.Consultation;
import com.workhelper.domain.consultation.repository.ConsultationRepository;
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
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final LaborCaseRepository laborCaseRepository;

    public List<ConsultationResponseDto> getMessages(Long caseId) {
        List<Consultation> consultations =
                consultationRepository.findByLaborCaseIdOrderByIdAsc(caseId);

        return consultations.stream()
                .map(ConsultationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultationResponseDto sendMessage(
            Long caseId,
            ConsultationRequestDto requestDto
    ) {
        LaborCase laborCase = laborCaseRepository.findById(caseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + caseId
                        )
                );

        Consultation userMessage = Consultation.builder()
                .laborCase(laborCase)
                .senderType("USER")
                .content(requestDto.getContent())
                .build();

        consultationRepository.save(userMessage);

        String aiAnswer = "AI 답변 예시입니다.";
        String structuredResult =
                "{\"summary\": \"구조화된 결과 예시\"}";

        Consultation aiMessage = Consultation.builder()
                .laborCase(laborCase)
                .senderType("AI")
                .content(aiAnswer)
                .structuredResult(structuredResult)
                .build();

        Consultation savedAiMessage =
                consultationRepository.save(aiMessage);

        return new ConsultationResponseDto(savedAiMessage);
    }
}