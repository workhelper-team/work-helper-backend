package com.workhelper.global.error;

/**
 * 모든 도메인의 비즈니스 예외가 상속받는 공통 예외.
 * ErrorCode를 들고 있어서, GlobalExceptionHandler가 이 코드 하나만 보고
 * HTTP Status + 오류코드를 결정할 수 있음.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}