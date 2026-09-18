//증거 업로드(API-EVD-001) 성공 시 클라이언트(프론트엔드)에게 반환하는 응답
package com.workhelper.domain.evidence.dto;

import java.time.OffsetDateTime;

public record EvidenceUploadResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String description,
        String analysisStatus,
        OffsetDateTime createdAt
) {
}