package com.api.automation.retry;

import com.api.automation.constants.Constants;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;

public class RetryPolicy {

    private static final Set<Integer> RETRYABLE_STATUS_CODES = new HashSet<>();
    private static final Set<Class<? extends Exception>> RETRYABLE_EXCEPTIONS = new HashSet<>();

    static {
        // Transient HTTP status codes
        RETRYABLE_STATUS_CODES.add(Constants.STATUS_BAD_GATEWAY);
        RETRYABLE_STATUS_CODES.add(Constants.STATUS_SERVICE_UNAVAILABLE);
        RETRYABLE_STATUS_CODES.add(Constants.STATUS_GATEWAY_TIMEOUT);

        // Network-related exceptions
        RETRYABLE_EXCEPTIONS.add(ConnectException.class);
        RETRYABLE_EXCEPTIONS.add(SocketTimeoutException.class);
    }

    private RetryPolicy() {
        throw new UnsupportedOperationException("RetryPolicy class cannot be instantiated");
    }

    public static boolean isRetryableStatusCode(int statusCode) {
        return RETRYABLE_STATUS_CODES.contains(statusCode);
    }

    public static boolean isRetryableException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        for (Class<? extends Exception> exceptionClass : RETRYABLE_EXCEPTIONS) {
            if (exceptionClass.isInstance(throwable)) {
                return true;
            }
        }

        // Check cause if present
        if (throwable.getCause() != null) {
            return isRetryableException(throwable.getCause());
        }

        return false;
    }

    public static boolean shouldRetry(Throwable throwable, Integer statusCode) {
        // Never retry assertion failures
        if (throwable instanceof AssertionError) {
            return false;
        }

        // Check status code
        if (statusCode != null && isRetryableStatusCode(statusCode)) {
            return true;
        }

        // Check exception type
        return isRetryableException(throwable);
    }
}
