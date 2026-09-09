package com.workhelper.domain.cases.dto;

import com.workhelper.domain.cases.entity.Case;

import java.time.LocalDateTime;

public record CaseResponse(
        Long id,
        Long userId,
        String title,
        String description,
        String caseType,
        String status,
        LocalDateTime createdAt
) {
    public static CaseResponse from(Case caseEntity) {
        return new CaseResponse(
                caseEntity.getId(),
                caseEntity.getUser().getId(),
                caseEntity.getTitle(),
                caseEntity.getDescription(),
                caseEntity.getCaseType(),
                caseEntity.getStatus().name(),
                caseEntity.getCreatedAt()
        );
    }
}
