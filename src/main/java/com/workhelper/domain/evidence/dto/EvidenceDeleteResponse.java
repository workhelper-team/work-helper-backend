package com.workhelper.domain.evidence.dto;

/**
 * API-EVD-005 응답
 * 명세: 삭제 결과
 */
public record EvidenceDeleteResponse(
        Long evidenceId,
        String status
) {
}