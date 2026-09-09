package com.workhelper.domain.petition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 진정서 생성 요청 DTO
 *
 * @param facts OCR/RAG 등으로 추출된 사건 사실관계 데이터
 */
public record PetitionCreateRequest(

        @NotNull(message = "사건 ID는 필수입니다.")
        Long caseId,

        @NotBlank(message = "사건 사실관계 데이터는 필수입니다.")
        String facts
) {
}
