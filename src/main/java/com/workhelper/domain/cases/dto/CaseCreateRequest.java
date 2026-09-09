package com.workhelper.domain.cases.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CaseCreateRequest(

        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotBlank(message = "사건 제목은 필수입니다.")
        String title,

        String description,

        String caseType
) {
}
