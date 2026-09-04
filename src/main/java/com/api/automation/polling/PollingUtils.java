package com.api.automation.polling;

import com.api.automation.constants.Constants;
import com.api.automation.exceptions.ApiFrameworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;
import java.util.function.Predicate;

public class PollingUtils {

    private static final Logger logger = LoggerFactory.getLogger(PollingUtils.class);

    private PollingUtils() {
        throw new UnsupportedOperationException("PollingUtils class cannot be instantiated");
    }

    public static <T> T poll(Supplier<T> supplier, Predicate<T> condition) {
        return poll(supplier, condition, Constants.POLLING_INTERVAL, Constants.MAX_POLLING_ATTEMPTS);
    }

    public static <T> T poll(Supplier<T> supplier, Predicate<T> condition, int intervalMs, int maxAttempts) {
        logger.info("Starting polling with interval {} ms, max attempts: {}", intervalMs, maxAttempts);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            logger.debug("Polling attempt {}/{}", attempt, maxAttempts);

            try {
                T result = supplier.get();

                if (condition.test(result)) {
                    logger.info("Polling condition met after {} attempts", attempt);
                    return result;
                }

                if (attempt < maxAttempts) {
                    Thread.sleep(intervalMs);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ApiFrameworkException("Polling interrupted", e);
            } catch (Exception e) {
                logger.warn("Error during polling attempt {}: {}", attempt, e.getMessage());
                if (attempt == maxAttempts) {
                    throw new ApiFrameworkException("Polling failed after " + maxAttempts + " attempts", e);
                }
                try {
                    Thread.sleep(intervalMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ApiFrameworkException("Polling interrupted", ie);
                }
            }
        }

        throw new ApiFrameworkException(
            String.format("Polling condition not met after %d attempts (%d ms total)",
                maxAttempts, maxAttempts * intervalMs)
        );
    }

    public static void waitForCondition(Runnable action, Supplier<Boolean> condition, int intervalMs, int maxAttempts) {
        poll(() -> {
            action.run();
            return condition.get();
        }, result -> result, intervalMs, maxAttempts);
    }
}
