package com.api.automation.constants;

public final class ApiEndpoints {

    private ApiEndpoints() {
        throw new UnsupportedOperationException("ApiEndpoints class cannot be instantiated");
    }

    // Base Paths
    public static final String BASE_PATH = "/api/v1";

    // User Endpoints
    public static final String USERS = BASE_PATH + "/users";
    public static final String USER_BY_ID = BASE_PATH + "/users/{id}";
    public static final String USER_SEARCH = BASE_PATH + "/users/search";

    // Authentication Endpoints
    public static final String AUTH_LOGIN = BASE_PATH + "/auth/login";
    public static final String AUTH_LOGOUT = BASE_PATH + "/auth/logout";
    public static final String AUTH_REFRESH = BASE_PATH + "/auth/refresh";
    public static final String AUTH_VALIDATE = BASE_PATH + "/auth/validate";

    // Order Endpoints
    public static final String ORDERS = BASE_PATH + "/orders";
    public static final String ORDER_BY_ID = BASE_PATH + "/orders/{id}";
    public static final String ORDER_STATUS = BASE_PATH + "/orders/{id}/status";

    // Health Check
    public static final String HEALTH = "/health";
    public static final String READY = "/ready";
}
