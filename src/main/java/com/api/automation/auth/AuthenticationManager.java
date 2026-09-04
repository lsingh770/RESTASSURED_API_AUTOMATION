package com.api.automation.auth;

import com.api.automation.config.EnvironmentConfig;
import com.api.automation.constants.ApiEndpoints;
import com.api.automation.constants.Constants;
import com.api.automation.exceptions.ApiFrameworkException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class AuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);
    private static final String TOKEN_CACHE_KEY = "auth_token";
    private static final EnvironmentConfig config = EnvironmentConfig.load();

    private AuthenticationManager() {
        throw new UnsupportedOperationException("AuthenticationManager class cannot be instantiated");
    }

    public static String getAccessToken() {
        String cachedToken = TokenCache.get(TOKEN_CACHE_KEY);
        if (cachedToken != null) {
            logger.debug("Using cached access token");
            return cachedToken;
        }

        logger.info("No valid cached token found. Authenticating...");
        return authenticate();
    }

    private static String authenticate() {
        if (!config.isAuthenticationConfigured()) {
            throw new ApiFrameworkException("Authentication credentials not configured");
        }

        try {
            Response response = RestAssured.given()
                .baseUri(config.getAuthUrl())
                .contentType("application/json")
                .body(buildAuthRequest())
                .post(ApiEndpoints.AUTH_LOGIN);

            if (response.getStatusCode() != Constants.STATUS_OK) {
                throw new ApiFrameworkException(
                    "Authentication failed with status: " + response.getStatusCode() +
                    ", body: " + response.getBody().asString()
                );
            }

            String accessToken = response.jsonPath().getString("access_token");
            Integer expiresIn = response.jsonPath().getInt("expires_in");

            if (accessToken == null) {
                throw new ApiFrameworkException("Access token not found in authentication response");
            }

            long expiryTimestamp = calculateExpiryTimestamp(expiresIn);
            TokenCache.put(TOKEN_CACHE_KEY, accessToken, expiryTimestamp);

            logger.info("Authentication successful. Token cached.");
            return accessToken;

        } catch (Exception e) {
            logger.error("Authentication failed", e);
            throw new ApiFrameworkException("Authentication failed: " + e.getMessage(), e);
        }
    }

    private static String buildAuthRequest() {
        if (config.getUsername() != null && config.getPassword() != null) {
            return String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                config.getUsername(), config.getPassword());
        } else if (config.getApiKey() != null && config.getApiSecret() != null) {
            return String.format("{\"api_key\":\"%s\",\"api_secret\":\"%s\"}",
                config.getApiKey(), config.getApiSecret());
        }
        throw new ApiFrameworkException("No valid authentication credentials configured");
    }

    private static long calculateExpiryTimestamp(Integer expiresIn) {
        if (expiresIn == null) {
            expiresIn = 3600; // Default to 1 hour
        }
        // Subtract buffer time to refresh before actual expiry
        long expirySeconds = expiresIn - Constants.TOKEN_EXPIRY_BUFFER_SECONDS;
        return Instant.now().getEpochSecond() + expirySeconds;
    }

    public static void invalidateToken() {
        TokenCache.invalidate(TOKEN_CACHE_KEY);
        logger.info("Token invalidated");
    }

    public static void clearAllTokens() {
        TokenCache.clear();
        logger.info("All tokens cleared");
    }
}
