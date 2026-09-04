package com.api.automation.retry;

import com.api.automation.config.EnvironmentConfig;
import com.api.automation.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private static final EnvironmentConfig config = EnvironmentConfig.load();
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (!config.isRetryEnabled()) {
            return false;
        }

        if (retryCount >= config.getMaxRetryAttempts()) {
            logger.warn("Max retry attempts ({}) reached for test: {}",
                config.getMaxRetryAttempts(), result.getName());
            return false;
        }

        Throwable throwable = result.getThrowable();
        Integer statusCode = extractStatusCode(result);

        boolean shouldRetry = RetryPolicy.shouldRetry(throwable, statusCode);

        if (shouldRetry) {
            retryCount++;
            logger.info("Retrying test '{}' - Attempt {}/{}",
                result.getName(), retryCount, config.getMaxRetryAttempts());

            // Add delay between retries
            try {
                Thread.sleep(config.getRetryDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Retry delay interrupted", e);
            }

            return true;
        }

        logger.debug("Test '{}' failure is not retryable", result.getName());
        return false;
    }

    private Integer extractStatusCode(ITestResult result) {
        // Attempt to extract status code from test context or exception message
        Object statusCodeAttr = result.getAttribute("statusCode");
        if (statusCodeAttr instanceof Integer) {
            return (Integer) statusCodeAttr;
        }
        return null;
    }
}
