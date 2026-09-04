package com.api.automation.constants;

public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // Configuration Keys
    public static final String ENV_KEY = "env";
    public static final String DEFAULT_ENV = "qa";

    // Timeouts (milliseconds)
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    public static final int DEFAULT_RESPONSE_TIMEOUT = 30000;
    public static final int POLLING_INTERVAL = 2000;
    public static final int MAX_POLLING_ATTEMPTS = 30;

    // Headers
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_USER_AGENT = "User-Agent";

    // Content Types
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";

    // HTTP Status Codes
    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_ACCEPTED = 202;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_UNAUTHORIZED = 401;
    public static final int STATUS_FORBIDDEN = 403;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_CONFLICT = 409;
    public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_BAD_GATEWAY = 502;
    public static final int STATUS_SERVICE_UNAVAILABLE = 503;
    public static final int STATUS_GATEWAY_TIMEOUT = 504;

    // Retry Configuration
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final int RETRY_DELAY_MS = 1000;

    // JIRA Configuration
    public static final String JIRA_CUSTOM_FIELD_SIGNATURE = "customfield_10100";
    public static final String JIRA_CUSTOM_FIELD_ENVIRONMENT = "customfield_10101";
    public static final String JIRA_CUSTOM_FIELD_TEST_NAME = "customfield_10102";
    public static final String JIRA_LABEL_AUTOMATION = "API-Automation";

    // Logging
    public static final String LOG_REQUEST_PREFIX = "REQUEST";
    public static final String LOG_RESPONSE_PREFIX = "RESPONSE";

    // Sensitive Fields to Mask
    public static final String[] SENSITIVE_FIELDS = {
        "password", "token", "secret", "apikey", "authorization",
        "access_token", "refresh_token", "client_secret", "api_key",
        "bearer", "auth", "credential", "apiToken"
    };

    // Schema Paths
    public static final String SCHEMA_BASE_PATH = "schemas/";

    // Test Data Paths
    public static final String TEST_DATA_BASE_PATH = "testdata/";

    // Payload Paths
    public static final String PAYLOAD_BASE_PATH = "payloads/";

    // Token Cache
    public static final long TOKEN_EXPIRY_BUFFER_SECONDS = 300; // 5 minutes before actual expiry

    // User Agent
    public static final String USER_AGENT_VALUE = "API-Automation-Framework/1.0";
}
