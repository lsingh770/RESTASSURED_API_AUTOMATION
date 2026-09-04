package com.api.automation.base;

import com.api.automation.config.EnvironmentConfig;
import com.api.automation.constants.Constants;
import com.api.automation.context.TestContext;
import com.api.automation.logging.LogFilter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class SpecificationFactory {

    private static final ThreadLocal<RequestSpecification> baseSpec = new ThreadLocal<>();
    private static final EnvironmentConfig config = EnvironmentConfig.load();

    private SpecificationFactory() {
        throw new UnsupportedOperationException("SpecificationFactory class cannot be instantiated");
    }

    public static RequestSpecification getBaseRequestSpec() {
        if (baseSpec.get() == null) {
            baseSpec.set(createBaseRequestSpec());
        }
        return baseSpec.get();
    }

    private static RequestSpecification createBaseRequestSpec() {
        TestContext context = TestContext.get();

        RestAssuredConfig restConfig = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", config.getConnectionTimeout())
                .setParam("http.socket.timeout", config.getSocketTimeout())
                .setParam("http.connection-manager.timeout", config.getResponseTimeout()));

        RequestSpecBuilder builder = new RequestSpecBuilder()
            .setBaseUri(config.getBaseUrl())
            .setContentType(ContentType.JSON)
            .setAccept(ContentType.JSON)
            .addHeader(Constants.HEADER_CORRELATION_ID, context.getCorrelationId())
            .addHeader(Constants.HEADER_REQUEST_ID, context.getRequestId())
            .addHeader(Constants.HEADER_USER_AGENT, Constants.USER_AGENT_VALUE)
            .setConfig(restConfig)
            .addFilter(new LogFilter(config.isEnableRequestLogging(), config.isEnableResponseLogging()));

        if (config.isEnableRequestLogging()) {
            builder.log(LogDetail.ALL);
        }

        return builder.build();
    }

    public static RequestSpecification getAuthenticatedRequestSpec(String token) {
        return new RequestSpecBuilder()
            .addRequestSpecification(getBaseRequestSpec())
            .addHeader(Constants.HEADER_AUTHORIZATION, "Bearer " + token)
            .build();
    }

    public static RequestSpecification getBasicAuthRequestSpec(String username, String password) {
        return new RequestSpecBuilder()
            .addRequestSpecification(getBaseRequestSpec())
            .setAuth(io.restassured.RestAssured.preemptive().basic(username, password))
            .build();
    }

    public static RequestSpecification getApiKeyRequestSpec(String apiKey) {
        return new RequestSpecBuilder()
            .addRequestSpecification(getBaseRequestSpec())
            .addHeader("X-API-Key", apiKey)
            .build();
    }

    public static void reset() {
        baseSpec.remove();
    }
}
