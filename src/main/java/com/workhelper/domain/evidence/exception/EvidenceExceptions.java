package com.workhelper.domain.evidence.exception;

import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;

public class EvidenceExceptions {

    public static class CaseNotFoundException extends BusinessException {
        public CaseNotFoundException(Long caseId) {
            super(ErrorCode.NOT_FOUND, "사건을 찾을 수 없습니다. caseId=" + caseId);
        }
    }

    public static class CaseAccessDeniedException extends BusinessException {
        public CaseAccessDeniedException(Long caseId) {
            super(ErrorCode.FORBIDDEN, "해당 사건에 대한 접근 권한이 없습니다. caseId=" + caseId);
        }
    }

    public static class InvalidEvidenceFileException extends BusinessException {
        public InvalidEvidenceFileException(String message) {
            super(ErrorCode.INVALID_REQUEST, message);
        }
    }

    public static class EvidenceNotFoundException extends BusinessException {
        public EvidenceNotFoundException(Long evidenceId) {
            super(ErrorCode.NOT_FOUND, "증거를 찾을 수 없습니다. evidenceId=" + evidenceId);
        }
    }

    /**
     * API-EVD-002: FastAPI(AI Backend) 분석 실패 시 사용.
     * 공통 오류코드 표 502 UPSTREAM_ERROR 대응 (재시도 가능 안내 포함).
     */
    public static class EvidenceAnalysisFailedException extends BusinessException {
        public EvidenceAnalysisFailedException(Long evidenceId, Throwable cause) {
            super(ErrorCode.UPSTREAM_ERROR,
                    "증거 분석 처리에 실패했습니다. 잠시 후 다시 시도해주세요. evidenceId=" + evidenceId,
                    cause);
        }
    }

    /**
     * 파일 저장/삭제 등 저장소 I/O 오류.
     * 의도적으로 BusinessException을 상속하지 않음 — 공통 오류코드 표(400~409, 502)에 해당하는
     * 케이스가 아니라 서버 내부 오류(500)이므로, GlobalExceptionHandler의 Exception 처리기로 넘어감.
     */
    public static class EvidenceStorageException extends RuntimeException {
        public EvidenceStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}