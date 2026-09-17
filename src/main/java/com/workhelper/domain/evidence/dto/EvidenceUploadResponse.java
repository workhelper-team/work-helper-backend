//증거 업로드(API-EVD-001) 성공 시 클라이언트(프론트엔드)에게 반환하는 응답
//업로드된 evidenceId, 파일 정보, 초기 분석 상태(PENDING) 등을 포함
package com.workhelper.domain.evidence.dto;


/**
 * API-EVD-001 응답
 * 명세: evidenceId, 파일정보, analysisStatus
 */
public record EvidenceUploadResponse(
        Long evidenceId,
        String originalName,
        String mimeType,
        String analysisStatus
) {
}