package com.workhelper.domain.consultation.service;

import com.workhelper.domain.consultation.dto.ConsultationRequest;
import com.workhelper.domain.consultation.dto.ConsultationResponse;
import com.workhelper.domain.consultation.entity.Consultation;
import com.workhelper.domain.consultation.repository.ConsultationRepository;
import com.workhelper.domain.user.entity.User;
import com.workhelper.domain.user.repository.UserRepository;
import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;
import com.workhelper.infra.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final AiClient aiClient;

    /**
     * 챗봇 질문 접수 -> FastAPI RAG 질의 -> 답변 저장
     */
    @Transactional
    public ConsultationResponse ask(ConsultationRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // TODO: 실제 FastAPI 연동 시 주석 해제
        // AiResponseDto aiResponse = aiClient.ragQuery(
        //         AiRequestDto.of("RAG_QUERY", request.question()));
        // String answer = aiResponse.result();

        String answer = "AI 서버 연동 전 임시 응답입니다.";

        Consultation consultation = Consultation.builder()
                .user(user)
                .question(request.question())
                .answer(answer)
                .type(Consultation.ConsultationType.CHATBOT)
                .sessionId(request.sessionId())
                .build();

        return ConsultationResponse.from(consultationRepository.save(consultation));
    }

    public List<ConsultationResponse> getHistory(Long userId) {
        return consultationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ConsultationResponse::from)
                .toList();
    }

    public ConsultationResponse getConsultation(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONSULTATION_NOT_FOUND));
        return ConsultationResponse.from(consultation);
    }
}
