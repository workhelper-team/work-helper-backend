package com.workhelper.domain.consultation.dto;

import com.workhelper.domain.consultation.entity.Consultation;
import lombok.Getter;

@Getter
public class ConsultationResponseDto {

    private Long id;
    private String senderType;
    private String content;
    private String structuredResult;

    public ConsultationResponseDto(Consultation consultation) {
        this.id = consultation.getId();
        this.senderType = consultation.getSenderType();
        this.content = consultation.getContent();
        this.structuredResult = consultation.getStructuredResult();
    }
}