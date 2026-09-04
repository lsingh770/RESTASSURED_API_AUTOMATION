package com.api.automation.listeners;

import com.api.automation.constants.FailureCategory;
import com.api.automation.context.TestContext;
import com.api.automation.jira.JiraIntegration;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        logger.info("===== TEST STARTED: {} =====", testName);

        // Initialize test context for this thread
        TestContext.reset();

        // Add test metadata to Allure
        Allure.parameter("Test Class", result.getTestClass().getName());
        Allure.parameter("Test Method", testName);
        Allure.parameter("Environment", System.getProperty("env", "qa"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();

        logger.info("===== TEST PASSED: {} (Duration: {} ms) =====", testName, duration);

        // Add success metadata to Allure
        Allure.parameter("Status", "PASSED");
        Allure.parameter("Duration (ms)", duration);

        // Cleanup
        TestContext.reset();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        long duration = result.getEndMillis() - result.getStartMillis();
        Throwable throwable = result.getThrowable();

        logger.error("===== TEST FAILED: {} (Duration: {} ms) =====", testName, duration);

        // Extract response if available from test context
        Response response = getResponseFromContext();

        // Classify the failure
        FailureCategory category = FailureClassifier.classify(throwable, response);
        logger.error("Failure Category: {}", category);

        // Extract error details
        String errorMessage = FailureClassifier.extractErrorMessage(throwable);
        String stackTrace = getStackTrace(throwable);

        // Add failure metadata to Allure
        Allure.parameter("Status", "FAILED");
        Allure.parameter("Duration (ms)", duration);
        Allure.parameter("Failure Category", category.getDisplayName());

        // Attach error details to Allure
        Allure.addAttachment("Error Message", "text/plain", errorMessage, "txt");
        Allure.addAttachment("Stack Trace", "text/plain", stackTrace, "txt");

        // Store failure category for reporting
        result.setAttribute("failureCategory", category);
        result.setAttribute("errorMessage", errorMessage);

        // Extract endpoint from test if available
        String endpoint = extractEndpoint(result);

        // Trigger JIRA defect creation (async, non-blocking)
        String buildNumber = System.getenv("BUILD_NUMBER");
        JiraIntegration.handleTestFailure(
            testName,
            endpoint,
            category,
            errorMessage,
            "See Allure report for request details",
            "See Allure report for response details",
            buildNumber
        );

        // Cleanup
        TestContext.reset();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();

        logger.warn("===== TEST SKIPPED: {} =====", testName);

        if (throwable != null) {
            logger.warn("Skip Reason: {}", throwable.getMessage());
            Allure.parameter("Skip Reason", throwable.getMessage());
        }

        Allure.parameter("Status", "SKIPPED");

        // Cleanup
        TestContext.reset();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        logger.info("Test failed but within success percentage: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onStart(ITestContext context) {
        logger.info("========================================");
        logger.info("TEST SUITE STARTED: {}", context.getName());
        logger.info("Environment: {}", System.getProperty("env", "qa"));
        logger.info("Parallel Mode: {}", context.getSuite().getParallel());
        logger.info("Thread Count: {}", context.getSuite().getXmlSuite().getThreadCount());
        logger.info("========================================");
    }

    @Override
    public void onFinish(ITestContext context) {
        int total = context.getAllTestMethods().length;
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        long duration = context.getEndDate().getTime() - context.getStartDate().getTime();

        logger.info("========================================");
        logger.info("TEST SUITE FINISHED: {}", context.getName());
        logger.info("Total Tests: {}", total);
        logger.info("Passed: {}", passed);
        logger.info("Failed: {}", failed);
        logger.info("Skipped: {}", skipped);
        logger.info("Pass Rate: {}%", total > 0 ? (passed * 100.0 / total) : 0);
        logger.info("Duration: {} ms ({} seconds)", duration, duration / 1000);
        logger.info("========================================");

        // Cleanup
        JiraIntegration.shutdown();
    }

    private Response getResponseFromContext() {
        try {
            Object response = TestContext.get().getTestData("response");
            if (response instanceof Response) {
                return (Response) response;
            }
        } catch (Exception e) {
            logger.debug("Could not extract response from context", e);
        }
        return null;
    }

    private String getStackTrace(Throwable throwable) {
        if (throwable == null) {
            return "No stack trace available";
        }

        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(throwable.toString()).append("\n");

        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.append("\tat ").append(element.toString()).append("\n");
        }

        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            stackTrace.append("Caused by: ").append(getStackTrace(throwable.getCause()));
        }

        return stackTrace.toString();
    }

    private String extractEndpoint(ITestResult result) {
        try {
            Object endpoint = result.getAttribute("endpoint");
            if (endpoint != null) {
                return endpoint.toString();
            }

            // Try to extract from method annotation
            if (result.getMethod().getConstructorOrMethod().getMethod().isAnnotationPresent(
                com.api.automation.annotations.ApiTest.class)) {
                com.api.automation.annotations.ApiTest annotation =
                    result.getMethod().getConstructorOrMethod().getMethod()
                        .getAnnotation(com.api.automation.annotations.ApiTest.class);
                return annotation.endpoint();
            }
        } catch (Exception e) {
            logger.debug("Could not extract endpoint", e);
        }
        return "N/A";
    }
}
