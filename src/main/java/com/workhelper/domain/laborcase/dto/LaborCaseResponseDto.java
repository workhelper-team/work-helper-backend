package com.workhelper.domain.laborcase.dto;

import com.workhelper.domain.laborcase.entity.LaborCase;
import lombok.Getter;

@Getter
public class LaborCaseResponseDto {
    private Long id;
    private String title;
    private String category;
    private String status;
    private String summary;

    public LaborCaseResponseDto(LaborCase laborCase) {
        this.id = laborCase.getId();
        this.title = laborCase.getTitle();
        this.category = laborCase.getCategory();
        this.status = laborCase.getStatus();
        this.summary = laborCase.getSummary();
    }
}