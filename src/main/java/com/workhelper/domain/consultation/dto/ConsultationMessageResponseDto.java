package com.workhelper.domain.consultation.dto;

import com.workhelper.domain.consultation.entity.ConsultationMessage;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class ConsultationMessageResponseDto {

    private final Long messageId;
    private final String role;
    private final String content;
    private final String structuredResult;
    private final OffsetDateTime createdAt;

    public ConsultationMessageResponseDto(ConsultationMessage message) {
        this.messageId = message.getId();
        this.role = message.getRole().name();
        this.content = message.getContent();
        this.structuredResult = message.getStructuredResult();
        this.createdAt = message.getCreatedAt();
    }
}