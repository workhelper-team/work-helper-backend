package com.workhelper.domain.file.dto;

import com.workhelper.domain.file.entity.FileMetadata;

import java.time.LocalDateTime;

public record FileResponse(
        Long id,
        Long caseId,
        String originalFileName,
        String filePath,
        Long fileSize,
        String contentType,
        LocalDateTime createdAt
) {
    public static FileResponse from(FileMetadata metadata) {
        return new FileResponse(
                metadata.getId(),
                metadata.getCaseEntity() != null ? metadata.getCaseEntity().getId() : null,
                metadata.getOriginalFileName(),
                metadata.getFilePath(),
                metadata.getFileSize(),
                metadata.getContentType(),
                metadata.getCreatedAt()
        );
    }
}
