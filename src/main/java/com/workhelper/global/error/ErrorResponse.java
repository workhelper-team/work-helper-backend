package com.workhelper.global.error;

/**
 * 공통 오류 응답 형식.
 * TODO: "오류 응답" 항목이 아직 추후확정 상태 — 최종 형식(필드명, timestamp 포함 여부 등)
 *       확정되면 이 파일만 고치면 됨 (다른 코드는 ErrorCode/BusinessException만 참조하므로 영향 없음).
 */
public record ErrorResponse(
        int status,
        String code,
        String message
) {
    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message);
    }
}