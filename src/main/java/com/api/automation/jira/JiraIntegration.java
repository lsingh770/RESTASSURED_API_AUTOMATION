package com.api.automation.jira;

import com.api.automation.config.EnvironmentConfig;
import com.api.automation.constants.FailureCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JiraIntegration {

    private static final Logger logger = LoggerFactory.getLogger(JiraIntegration.class);
    private static final EnvironmentConfig config = EnvironmentConfig.load();
    private static JiraClient jiraClient;

    static {
        if (config.isJiraConfigured() && config.isJiraAutoCreate()) {
            jiraClient = new JiraClient(config);
        }
    }

    private JiraIntegration() {
        throw new UnsupportedOperationException("JiraIntegration class cannot be instantiated");
    }

    public static void handleTestFailure(
        String testName,
        String endpoint,
        FailureCategory category,
        String errorMessage,
        String requestDetails,
        String responseDetails,
        String buildNumber
    ) {

        if (!config.isJiraConfigured() || !config.isJiraAutoCreate()) {
            logger.info("JIRA integration not configured or disabled. Skipping defect creation.");
            return;
        }

        // Run JIRA operations asynchronously to not block test execution
        CompletableFuture.runAsync(() -> {
            try {
                createOrUpdateDefect(testName, endpoint, category, errorMessage, requestDetails, responseDetails, buildNumber);
            } catch (Exception e) {
                logger.error("JIRA integration failed (test result not affected): {}", e.getMessage(), e);
            }
        });
    }

    private static void createOrUpdateDefect(
        String testName,
        String endpoint,
        FailureCategory category,
        String errorMessage,
        String requestDetails,
        String responseDetails,
        String buildNumber
    ) {

        String signature = FailureSignatureGenerator.generateSignature(testName, endpoint, category, errorMessage);

        if (config.isJiraDuplicateCheck()) {
            List<String> existingIssues = jiraClient.searchIssueBySignature(signature);

            if (!existingIssues.isEmpty()) {
                String existingIssueKey = existingIssues.get(0);
                logger.info("Found existing JIRA issue: {}. Updating with latest execution details.", existingIssueKey);

                String updateComment = buildUpdateComment(buildNumber, errorMessage);
                jiraClient.updateIssue(existingIssueKey, updateComment);

                return;
            }
        }

        // No existing issue found, create new one
        String summary = buildSummary(testName, category);
        String description = buildDescription(
            testName,
            endpoint,
            category,
            errorMessage,
            requestDetails,
            responseDetails,
            buildNumber,
            signature
        );

        String issueKey = jiraClient.createIssue(summary, description, category, signature);
        logger.info("Created new JIRA defect: {}", issueKey);
    }

    private static String buildSummary(String testName, FailureCategory category) {
        return String.format("[API Automation][%s] %s - %s",
            config.getEnvironment().toUpperCase(),
            testName,
            category.getDisplayName()
        );
    }

    private static String buildDescription(
        String testName,
        String endpoint,
        FailureCategory category,
        String errorMessage,
        String requestDetails,
        String responseDetails,
        String buildNumber,
        String signature
    ) {

        StringBuilder description = new StringBuilder();

        description.append("h2. Test Failure Details\n\n");
        description.append("*Environment:* ").append(config.getEnvironment().toUpperCase()).append("\n");
        description.append("*Test Name:* ").append(testName).append("\n");
        description.append("*Endpoint:* ").append(endpoint != null ? endpoint : "N/A").append("\n");
        description.append("*Failure Category:* ").append(category.getDisplayName()).append("\n");
        description.append("*Build Number:* ").append(buildNumber != null ? buildNumber : "N/A").append("\n");
        description.append("*Timestamp:* ").append(LocalDateTime.now()).append("\n");
        description.append("*Signature:* ").append(signature).append("\n\n");

        description.append("h2. Error Message\n\n");
        description.append("{code}\n");
        description.append(errorMessage != null ? errorMessage : "No error message available");
        description.append("\n{code}\n\n");

        if (requestDetails != null && !requestDetails.isEmpty()) {
            description.append("h2. Request Details\n\n");
            description.append("{code}\n");
            description.append(requestDetails);
            description.append("\n{code}\n\n");
        }

        if (responseDetails != null && !responseDetails.isEmpty()) {
            description.append("h2. Response Details\n\n");
            description.append("{code}\n");
            description.append(responseDetails);
            description.append("\n{code}\n\n");
        }

        description.append("h2. Additional Information\n\n");
        description.append("This defect was automatically created by the API Automation Framework.\n");
        description.append("Framework Version: 1.0.0\n");

        return description.toString();
    }

    private static String buildUpdateComment(String buildNumber, String errorMessage) {
        StringBuilder comment = new StringBuilder();

        comment.append("Test failed again in build: ").append(buildNumber != null ? buildNumber : "N/A").append("\n");
        comment.append("Timestamp: ").append(LocalDateTime.now()).append("\n\n");
        comment.append("Error Message:\n");
        comment.append("{code}\n");
        comment.append(errorMessage != null ? errorMessage : "No error message available");
        comment.append("\n{code}");

        return comment.toString();
    }

    public static void shutdown() {
        if (jiraClient != null) {
            jiraClient.close();
        }
    }
}
