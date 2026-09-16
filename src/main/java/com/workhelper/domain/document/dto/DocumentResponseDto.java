package com.workhelper.domain.document.dto;

import com.workhelper.domain.document.entity.Document;
import lombok.Getter;

@Getter
public class DocumentResponseDto {

    private Long id;
    private String title;
    private String fileName;
    private String fileUrl;

    public DocumentResponseDto(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.fileName = document.getFileName();
        this.fileUrl = document.getFileUrl();
    }
}