package com.api.automation.listeners;

import com.api.automation.constants.Constants;
import com.api.automation.constants.FailureCategory;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class FailureClassifier {

    private static final Logger logger = LoggerFactory.getLogger(FailureClassifier.class);

    private FailureClassifier() {
        throw new UnsupportedOperationException("FailureClassifier class cannot be instantiated");
    }

    public static FailureCategory classify(Throwable throwable, Response response) {
        logger.debug("Classifying failure - Throwable: {}, Response: {}",
            throwable != null ? throwable.getClass().getSimpleName() : "null",
            response != null ? response.getStatusCode() : "null");

        // Check status code first
        if (response != null) {
            FailureCategory statusCategory = classifyByStatusCode(response.getStatusCode());
            if (statusCategory != null) {
                return statusCategory;
            }
        }

        // Then check exception type
        if (throwable != null) {
            FailureCategory exceptionCategory = classifyByException(throwable);
            if (exceptionCategory != null) {
                return exceptionCategory;
            }
        }

        // Default
        return FailureCategory.UNKNOWN_FAILURE;
    }

    private static FailureCategory classifyByStatusCode(int statusCode) {
        if (statusCode == Constants.STATUS_UNAUTHORIZED) {
            return FailureCategory.AUTHENTICATION_FAILURE;
        } else if (statusCode == Constants.STATUS_FORBIDDEN) {
            return FailureCategory.AUTHORIZATION_FAILURE;
        } else if (statusCode == Constants.STATUS_BAD_REQUEST ||
                   statusCode == Constants.STATUS_NOT_FOUND ||
                   statusCode == Constants.STATUS_METHOD_NOT_ALLOWED ||
                   statusCode == Constants.STATUS_CONFLICT ||
                   statusCode == Constants.STATUS_UNPROCESSABLE_ENTITY) {
            return FailureCategory.FUNCTIONAL_FAILURE;
        } else if (statusCode == Constants.STATUS_BAD_GATEWAY ||
                   statusCode == Constants.STATUS_SERVICE_UNAVAILABLE ||
                   statusCode == Constants.STATUS_GATEWAY_TIMEOUT) {
            return FailureCategory.ENVIRONMENT_FAILURE;
        } else if (statusCode == Constants.STATUS_INTERNAL_SERVER_ERROR) {
            return FailureCategory.ENVIRONMENT_FAILURE;
        }
        return null;
    }

    private static FailureCategory classifyByException(Throwable throwable) {
        String exceptionClassName = throwable.getClass().getSimpleName();
        String message = throwable.getMessage() != null ? throwable.getMessage().toLowerCase() : "";

        // Assertion failures
        if (throwable instanceof AssertionError) {
            if (message.contains("schema")) {
                return FailureCategory.SCHEMA_VALIDATION_FAILURE;
            } else if (message.contains("status code")) {
                return FailureCategory.STATUS_CODE_FAILURE;
            } else if (message.contains("body") || message.contains("json")) {
                return FailureCategory.RESPONSE_BODY_FAILURE;
            }
            return FailureCategory.ASSERTION_FAILURE;
        }

        // Network failures
        if (throwable instanceof ConnectException ||
            throwable instanceof UnknownHostException) {
            return FailureCategory.CONNECTION_FAILURE;
        }

        // Timeout
        if (throwable instanceof SocketTimeoutException ||
            message.contains("timeout") ||
            message.contains("timed out")) {
            return FailureCategory.TIMEOUT;
        }

        // Configuration issues
        if (message.contains("configuration") ||
            message.contains("config") ||
            exceptionClassName.contains("Configuration")) {
            return FailureCategory.CONFIGURATION_FAILURE;
        }

        // Test data issues
        if (message.contains("test data") ||
            message.contains("testdata")) {
            return FailureCategory.TEST_DATA_FAILURE;
        }

        // JIRA issues
        if (message.contains("jira")) {
            return FailureCategory.JIRA_FAILURE;
        }

        // Framework issues
        if (exceptionClassName.contains("Framework")) {
            return FailureCategory.FRAMEWORK_FAILURE;
        }

        // Check cause if present
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            return classifyByException(throwable.getCause());
        }

        return FailureCategory.UNKNOWN_FAILURE;
    }

    public static String extractErrorMessage(Throwable throwable) {
        if (throwable == null) {
            return "No error message available";
        }

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(throwable.getClass().getSimpleName());

        if (throwable.getMessage() != null) {
            errorMessage.append(": ").append(throwable.getMessage());
        }

        // Include cause if available
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            errorMessage.append("\nCaused by: ").append(extractErrorMessage(throwable.getCause()));
        }

        return errorMessage.toString();
    }
}
