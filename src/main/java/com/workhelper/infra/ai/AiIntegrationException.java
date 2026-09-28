package com.workhelper.infra.ai;

public class AiIntegrationException extends RuntimeException {

    public enum Kind {
        BAD_REQUEST,
        SERVER_ERROR,
        HTTP_ERROR,
        CONNECTION_FAILURE,
        TIMEOUT,
        INVALID_RESPONSE
    }

    private final Kind kind;
    private final Integer statusCode;

    public AiIntegrationException(Kind kind, Integer statusCode, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.statusCode = statusCode;
    }

    public Kind getKind() {
        return kind;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
