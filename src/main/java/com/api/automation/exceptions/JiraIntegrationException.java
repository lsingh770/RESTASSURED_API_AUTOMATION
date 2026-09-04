package com.api.automation.exceptions;

public class JiraIntegrationException extends ApiFrameworkException {

    private static final long serialVersionUID = 1L;

    public JiraIntegrationException(String message) {
        super(message);
    }

    public JiraIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
