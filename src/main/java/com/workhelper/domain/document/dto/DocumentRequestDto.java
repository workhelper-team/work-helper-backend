package com.workhelper.domain.document.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DocumentRequestDto {

    private String title;
    private String fileName;
    private String fileUrl;
}