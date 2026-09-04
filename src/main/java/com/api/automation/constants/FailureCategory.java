package com.api.automation.constants;

public enum FailureCategory {
    FUNCTIONAL_FAILURE("Functional Failure", "Business logic or functional requirement not met"),
    ASSERTION_FAILURE("Assertion Failure", "Assertion condition failed"),
    STATUS_CODE_FAILURE("Status Code Failure", "HTTP status code mismatch"),
    SCHEMA_VALIDATION_FAILURE("Schema Validation Failure", "JSON schema validation failed"),
    RESPONSE_BODY_FAILURE("Response Body Failure", "Response body content mismatch"),
    REQUEST_FAILURE("Request Failure", "Failed to send request"),
    AUTHENTICATION_FAILURE("Authentication Failure", "Authentication failed"),
    AUTHORIZATION_FAILURE("Authorization Failure", "Authorization/permission denied"),
    TIMEOUT("Timeout", "Request or operation timed out"),
    CONNECTION_FAILURE("Connection Failure", "Network connection failed"),
    NETWORK_FAILURE("Network Failure", "Network-related failure"),
    DATA_FAILURE("Data Failure", "Test data issue"),
    CONFIGURATION_FAILURE("Configuration Failure", "Configuration or setup issue"),
    ENVIRONMENT_FAILURE("Environment Failure", "Environment availability issue"),
    TEST_DATA_FAILURE("Test Data Failure", "Test data generation or retrieval failed"),
    JIRA_FAILURE("JIRA Failure", "JIRA integration issue"),
    FRAMEWORK_FAILURE("Framework Failure", "Framework internal failure"),
    UNKNOWN_FAILURE("Unknown Failure", "Unclassified failure");

    private final String displayName;
    private final String description;

    FailureCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
