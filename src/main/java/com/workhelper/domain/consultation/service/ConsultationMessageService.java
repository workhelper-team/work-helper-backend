package com.workhelper.domain.consultation.service;

import com.workhelper.domain.consultation.dto.ConsultationMessageRequestDto;
import com.workhelper.domain.consultation.dto.ConsultationMessageResponseDto;
import com.workhelper.domain.consultation.entity.ConsultationMessage;
import com.workhelper.domain.consultation.repository.ConsultationMessageRepository;
import com.workhelper.domain.laborcase.entity.LaborCase;
import com.workhelper.domain.laborcase.repository.LaborCaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


// ============================================================
// 상담 메시지 Service
//
// 상담 메시지의 조회 및 저장을 담당
// Controller에서 전달받은 요청을 처리하고
// Repository를 통해 상담 메시지를 DB에 저장하거나 조회
// ============================================================
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationMessageService {

    // ============================================================
    // 상담 메시지 DB 접근을 담당하는 Repository
    // ============================================================
    private final ConsultationMessageRepository consultationMessageRepository;


    // ============================================================
    // 노동 사건 DB 접근을 담당하는 Repository
    //
    // 상담 메시지를 저장하기 전에
    // 해당 사건이 실제로 존재하는지 확인할 때 사용
    // ============================================================
    private final LaborCaseRepository laborCaseRepository;


    // ============================================================
    // 상담 메시지 조회
    //
    // 특정 사건에 연결된 상담 메시지 목록을 조회
    //
    // F-AI-007
    // 상담 메시지의 순서와 역할을 보존하고
    // 재조회 시 시간순으로 구성
    // ============================================================
    public List<ConsultationMessageResponseDto> getMessages(
            Long caseId
    ) {

        // --------------------------------------------------------
        // 특정 사건에 연결된 메시지를 생성 시각 기준
        // 오름차순으로 조회
        //
        // 오래된 메시지 → 최신 메시지 순서
        // --------------------------------------------------------
        List<ConsultationMessage> messages =
                consultationMessageRepository
                        .findByLaborCase_IdOrderByCreatedAtAsc(caseId);


        // --------------------------------------------------------
        // 조회한 Entity 목록을
        // Response DTO 목록으로 변환
        // --------------------------------------------------------
        return messages.stream()
                .map(ConsultationMessageResponseDto::new)
                .collect(Collectors.toList());
    }


    // ============================================================
    // 상담 메시지 전송
    //
    // F-AI-001
    // 사용자가 입력한 자연어 상담 메시지를
    // 해당 사건의 상담 대화 이력에 저장
    //
    // 현재 단계에서는 사용자 메시지 저장까지 담당
    // 이후 AI 처리 파이프라인과 연결 가능
    // ============================================================
    @Transactional
    public ConsultationMessageResponseDto sendMessage(
            Long caseId,
            ConsultationMessageRequestDto requestDto
    ) {

        // --------------------------------------------------------
        // 1. 상담 대상 사건이 실제로 존재하는지 확인
        // --------------------------------------------------------
        LaborCase laborCase = laborCaseRepository.findById(caseId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 사건을 찾을 수 없습니다. ID: " + caseId
                        )
                );


        // --------------------------------------------------------
        // 2. 사용자가 입력한 상담 메시지 생성
        //
        // role은 설계서에서 정의한 USER 값을 사용
        // structuredResult는 사용자 메시지이므로 NULL
        // --------------------------------------------------------
        ConsultationMessage userMessage =
                ConsultationMessage.builder()
                        .laborCase(laborCase)
                        .role("USER")
                        .content(requestDto.getContent())
                        .structuredResult(null)
                        .build();


        // --------------------------------------------------------
        // 3. 사용자 메시지를 DB에 저장
        // --------------------------------------------------------
        ConsultationMessage savedMessage =
                consultationMessageRepository.save(userMessage);


        // --------------------------------------------------------
        // 4. 저장된 Entity를 Response DTO로 변환하여 반환
        // --------------------------------------------------------
        return new ConsultationMessageResponseDto(savedMessage);
    }
}