package com.api.automation.exceptions;

public class ApiFrameworkException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApiFrameworkException(String message) {
        super(message);
    }

    public ApiFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiFrameworkException(Throwable cause) {
        super(cause);
    }
}
