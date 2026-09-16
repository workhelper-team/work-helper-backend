package com.workhelper.domain.laborcase.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LaborCaseRequestDto {
    private String title;
    private String category;
    private String status;
    private String summary;
}