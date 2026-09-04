package com.api.automation.config;

import lombok.Getter;

@Getter
public class EnvironmentConfig {

    private final String baseUrl;
    private final String authUrl;
    private final int connectionTimeout;
    private final int socketTimeout;
    private final int responseTimeout;
    private final boolean enableRequestLogging;
    private final boolean enableResponseLogging;
    private final String logLevel;

    // Authentication
    private final String apiKey;
    private final String apiSecret;
    private final String username;
    private final String password;

    // JIRA Configuration
    private final String jiraBaseUrl;
    private final String jiraUsername;
    private final String jiraApiToken;
    private final String jiraProjectKey;
    private final boolean jiraAutoCreate;
    private final boolean jiraDuplicateCheck;
    private final boolean jiraUpdateExisting;

    // Retry Configuration
    private final int maxRetryAttempts;
    private final int retryDelayMs;
    private final boolean retryEnabled;

    // Parallel Execution
    private final int threadCount;
    private final String parallelMode;

    private final String environment;

    private EnvironmentConfig(ConfigManager configManager) {
        this.environment = configManager.getEnvironment();
        // API Configuration
        this.baseUrl = configManager.getProperty("api.base.url");
        this.authUrl = configManager.getProperty("api.auth.url", baseUrl);
        this.connectionTimeout = configManager.getIntProperty("api.connection.timeout", 30000);
        this.socketTimeout = configManager.getIntProperty("api.socket.timeout", 30000);
        this.responseTimeout = configManager.getIntProperty("api.response.timeout", 30000);

        // Logging Configuration
        this.enableRequestLogging = configManager.getBooleanProperty("logging.request.enabled", false);
        this.enableResponseLogging = configManager.getBooleanProperty("logging.response.enabled", false);
        this.logLevel = configManager.getProperty("logging.level", "INFO");

        // Authentication (from environment variables for security)
        this.apiKey = getSecureProperty("API_KEY", configManager, "api.key");
        this.apiSecret = getSecureProperty("API_SECRET", configManager, "api.secret");
        this.username = getSecureProperty("API_USERNAME", configManager, "api.username");
        this.password = getSecureProperty("API_PASSWORD", configManager, "api.password");

        // JIRA Configuration (from environment variables)
        this.jiraBaseUrl = getSecureProperty("JIRA_BASE_URL", configManager, "jira.base.url");
        this.jiraUsername = getSecureProperty("JIRA_USERNAME", configManager, "jira.username");
        this.jiraApiToken = getSecureProperty("JIRA_API_TOKEN", configManager, "jira.api.token");
        this.jiraProjectKey = configManager.getProperty("jira.project.key", "API");
        this.jiraAutoCreate = configManager.getBooleanProperty("jira.auto.create", true);
        this.jiraDuplicateCheck = configManager.getBooleanProperty("jira.duplicate.check", true);
        this.jiraUpdateExisting = configManager.getBooleanProperty("jira.update.existing", true);

        // Retry Configuration
        this.maxRetryAttempts = configManager.getIntProperty("retry.max.attempts", 3);
        this.retryDelayMs = configManager.getIntProperty("retry.delay.ms", 1000);
        this.retryEnabled = configManager.getBooleanProperty("retry.enabled", true);

        // Parallel Execution
        this.threadCount = configManager.getIntProperty("parallel.thread.count", 10);
        this.parallelMode = configManager.getProperty("parallel.mode", "methods");
    }

    private String getSecureProperty(String envVar, ConfigManager configManager, String propertyKey) {
        String value = System.getenv(envVar);
        if (value == null || value.trim().isEmpty()) {
            value = configManager.getProperty(propertyKey);
        }
        return value;
    }

    public static EnvironmentConfig load() {
        return new EnvironmentConfig(ConfigManager.getInstance());
    }

    public boolean isJiraConfigured() {
        return jiraBaseUrl != null && jiraUsername != null && jiraApiToken != null;
    }

    public boolean isAuthenticationConfigured() {
        return (apiKey != null && apiSecret != null) || (username != null && password != null);
    }

    public String getEnvironment() {
        return environment;
    }
}
