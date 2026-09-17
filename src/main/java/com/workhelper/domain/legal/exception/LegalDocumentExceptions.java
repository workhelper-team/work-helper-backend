package com.workhelper.domain.legal.exception;

import com.workhelper.global.error.BusinessException;
import com.workhelper.global.error.ErrorCode;

public class LegalDocumentExceptions {

    public static class LegalDocumentNotFoundException extends BusinessException {
        public LegalDocumentNotFoundException(Long legalDocumentId) {
            super(ErrorCode.NOT_FOUND, "법률자료를 찾을 수 없습니다. legalDocumentId=" + legalDocumentId);
        }
    }
}