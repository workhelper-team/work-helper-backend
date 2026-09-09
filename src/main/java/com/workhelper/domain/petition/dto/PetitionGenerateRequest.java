package com.workhelper.domain.petition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetitionGenerateRequest(

        @NotNull(message = "사건 ID는 필수입니다.")
        Long caseId,

        @NotBlank(message = "진정서 제목은 필수입니다.")
        String title,

        @NotBlank(message = "진정 요지(사실관계)는 필수입니다.")
        String facts
) {
}
