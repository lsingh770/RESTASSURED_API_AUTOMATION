package com.api.automation.logging;

import com.api.automation.context.TestContext;
import io.qameta.allure.Allure;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LogFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(LogFilter.class);
    private final boolean logRequests;
    private final boolean logResponses;

    public LogFilter(boolean logRequests, boolean logResponses) {
        this.logRequests = logRequests;
        this.logResponses = logResponses;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {

        TestContext testContext = TestContext.get();
        MDC.put("correlationId", testContext.getCorrelationId());
        MDC.put("requestId", testContext.getRequestId());

        if (logRequests) {
            logRequest(requestSpec);
        }

        long startTime = System.currentTimeMillis();
        Response response = ctx.next(requestSpec, responseSpec);
        long duration = System.currentTimeMillis() - startTime;

        if (logResponses) {
            logResponse(response, duration);
        }

        attachToAllure(requestSpec, response, duration);

        MDC.clear();
        return response;
    }

    private void logRequest(FilterableRequestSpecification requestSpec) {
        StringBuilder logMessage = new StringBuilder("\n========== REQUEST ==========\n");
        logMessage.append("Method: ").append(requestSpec.getMethod()).append("\n");
        logMessage.append("URI: ").append(requestSpec.getURI()).append("\n");

        if (!requestSpec.getHeaders().asList().isEmpty()) {
            logMessage.append("Headers:\n");
            requestSpec.getHeaders().asList().forEach(header -> {
                String value = header.getValue();
                if (header.getName().equalsIgnoreCase("Authorization")) {
                    value = MaskingUtils.maskAuthorizationHeader(value);
                }
                logMessage.append("  ").append(header.getName()).append(": ").append(value).append("\n");
            });
        }

        if (requestSpec.getBody() != null) {
            String body = requestSpec.getBody().toString();
            body = MaskingUtils.maskSensitiveData(body);
            logMessage.append("Body:\n").append(body).append("\n");
        }

        logMessage.append("=============================");
        logger.info(logMessage.toString());
    }

    private void logResponse(Response response, long duration) {
        StringBuilder logMessage = new StringBuilder("\n========== RESPONSE ==========\n");
        logMessage.append("Status: ").append(response.getStatusCode()).append(" ").append(response.getStatusLine()).append("\n");
        logMessage.append("Duration: ").append(duration).append(" ms\n");

        if (!response.getHeaders().asList().isEmpty()) {
            logMessage.append("Headers:\n");
            response.getHeaders().asList().forEach(header ->
                logMessage.append("  ").append(header.getName()).append(": ").append(header.getValue()).append("\n")
            );
        }

        String body = response.getBody().asString();
        if (body != null && !body.isEmpty()) {
            body = MaskingUtils.maskSensitiveData(body);
            logMessage.append("Body:\n").append(body).append("\n");
        }

        logMessage.append("==============================");
        logger.info(logMessage.toString());
    }

    private void attachToAllure(FilterableRequestSpecification requestSpec, Response response, long duration) {
        try {
            // Attach request details
            String requestDetails = buildRequestDetails(requestSpec);
            Allure.addAttachment("Request Details", "text/plain", requestDetails, "txt");

            // Attach response details
            String responseDetails = buildResponseDetails(response, duration);
            Allure.addAttachment("Response Details", "text/plain", responseDetails, "txt");

            // Attach request body if exists
            if (requestSpec.getBody() != null) {
                String body = MaskingUtils.maskSensitiveData(requestSpec.getBody().toString());
                Allure.addAttachment("Request Body", "application/json", body, "json");
            }

            // Attach response body if exists
            String responseBody = response.getBody().asString();
            if (responseBody != null && !responseBody.isEmpty()) {
                responseBody = MaskingUtils.maskSensitiveData(responseBody);
                Allure.addAttachment("Response Body", "application/json", responseBody, "json");
            }
        } catch (Exception e) {
            logger.warn("Failed to attach request/response to Allure report", e);
        }
    }

    private String buildRequestDetails(FilterableRequestSpecification requestSpec) {
        StringBuilder details = new StringBuilder();
        details.append("Method: ").append(requestSpec.getMethod()).append("\n");
        details.append("URI: ").append(requestSpec.getURI()).append("\n");
        details.append("Correlation ID: ").append(TestContext.get().getCorrelationId()).append("\n");
        details.append("Request ID: ").append(TestContext.get().getRequestId()).append("\n");
        details.append("\nHeaders:\n");
        requestSpec.getHeaders().asList().forEach(header -> {
            String value = header.getValue();
            if (header.getName().equalsIgnoreCase("Authorization")) {
                value = MaskingUtils.maskAuthorizationHeader(value);
            }
            details.append(header.getName()).append(": ").append(value).append("\n");
        });
        return details.toString();
    }

    private String buildResponseDetails(Response response, long duration) {
        StringBuilder details = new StringBuilder();
        details.append("Status Code: ").append(response.getStatusCode()).append("\n");
        details.append("Status Line: ").append(response.getStatusLine()).append("\n");
        details.append("Duration: ").append(duration).append(" ms\n");
        details.append("\nHeaders:\n");
        response.getHeaders().asList().forEach(header ->
            details.append(header.getName()).append(": ").append(header.getValue()).append("\n")
        );
        return details.toString();
    }
}
