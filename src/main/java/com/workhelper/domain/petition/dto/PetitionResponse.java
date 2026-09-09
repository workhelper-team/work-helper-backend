package com.workhelper.domain.petition.dto;

import com.workhelper.domain.petition.entity.Petition;

import java.time.LocalDateTime;

public record PetitionResponse(
        Long id,
        Long caseId,
        String title,
        String content,
        String status,
        LocalDateTime createdAt
) {
    public static PetitionResponse from(Petition petition) {
        return new PetitionResponse(
                petition.getId(),
                petition.getCaseEntity().getId(),
                petition.getTitle(),
                petition.getContent(),
                petition.getStatus().name(),
                petition.getCreatedAt()
        );
    }
}
