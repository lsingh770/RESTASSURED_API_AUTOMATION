package com.api.automation.clients;

import com.api.automation.constants.ApiEndpoints;
import com.api.automation.models.request.LoginRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.Map;

public class AuthApiClient extends BaseApiClient {

    @Step("Login user: {username}")
    public Response login(String username, String password) {
        logger.info("Logging in user: {}", username);
        LoginRequest request = LoginRequest.builder()
            .username(username)
            .password(password)
            .build();
        return post(ApiEndpoints.AUTH_LOGIN, request);
    }

    @Step("Login with request")
    public Response login(LoginRequest request) {
        logger.info("Logging in user: {}", request.getUsername());
        return post(ApiEndpoints.AUTH_LOGIN, request);
    }

    @Step("Logout user")
    public Response logout() {
        logger.info("Logging out user");
        return post(ApiEndpoints.AUTH_LOGOUT, null);
    }

    @Step("Refresh token")
    public Response refreshToken(String refreshToken) {
        logger.info("Refreshing token");
        return post(ApiEndpoints.AUTH_REFRESH, Map.of("refresh_token", refreshToken));
    }

    @Step("Validate token")
    public Response validateToken(String token) {
        logger.info("Validating token");
        return post(ApiEndpoints.AUTH_VALIDATE, Map.of("token", token));
    }
}
