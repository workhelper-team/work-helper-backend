package com.workhelper.global.security.session;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class SessionStoreUnavailableException extends RuntimeException {
    public SessionStoreUnavailableException(Throwable cause) {
        super("Session store unavailable", cause);
    }
}
