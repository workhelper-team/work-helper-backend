package com.workhelper.domain.document.dto;

import java.time.OffsetDateTime;

public record DocumentSummaryResponse(Long documentId, Long caseId, String documentType,
                                      String title, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
}
