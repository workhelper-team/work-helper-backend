package com.workhelper.domain.consultation.dto;

import com.workhelper.domain.consultation.entity.Consultation;

import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long userId,
        String question,
        String answer,
        String type,
        String sessionId,
        LocalDateTime createdAt
) {
    public static ConsultationResponse from(Consultation consultation) {
        return new ConsultationResponse(
                consultation.getId(),
                consultation.getUser().getId(),
                consultation.getQuestion(),
                consultation.getAnswer(),
                consultation.getType().name(),
                consultation.getSessionId(),
                consultation.getCreatedAt()
        );
    }
}
