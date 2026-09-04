package com.api.automation.validators;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseValidator {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidator.class);
    private final Response response;

    public ResponseValidator(Response response) {
        this.response = response;
    }

    @Step("Validate status code is {expectedStatusCode}")
    public ResponseValidator validateStatusCode(int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("Validating status code - Expected: {}, Actual: {}", expectedStatusCode, actualStatusCode);

        assertThat(actualStatusCode)
            .as("Status code validation")
            .isEqualTo(expectedStatusCode);

        return this;
    }

    @Step("Validate status code is in range {minStatusCode} to {maxStatusCode}")
    public ResponseValidator validateStatusCodeInRange(int minStatusCode, int maxStatusCode) {
        int actualStatusCode = response.getStatusCode();
        logger.info("Validating status code range - Expected: {}-{}, Actual: {}",
            minStatusCode, maxStatusCode, actualStatusCode);

        assertThat(actualStatusCode)
            .as("Status code range validation")
            .isBetween(minStatusCode, maxStatusCode);

        return this;
    }

    @Step("Validate response time is less than {maxResponseTime} ms")
    public ResponseValidator validateResponseTime(long maxResponseTime) {
        long actualResponseTime = response.getTime();
        logger.info("Validating response time - Max: {} ms, Actual: {} ms", maxResponseTime, actualResponseTime);

        assertThat(actualResponseTime)
            .as("Response time validation")
            .isLessThanOrEqualTo(maxResponseTime);

        return this;
    }

    @Step("Validate header {headerName} exists")
    public ResponseValidator validateHeaderExists(String headerName) {
        logger.info("Validating header exists: {}", headerName);

        assertThat(response.getHeader(headerName))
            .as("Header existence validation for: " + headerName)
            .isNotNull();

        return this;
    }

    @Step("Validate header {headerName} equals {expectedValue}")
    public ResponseValidator validateHeader(String headerName, String expectedValue) {
        String actualValue = response.getHeader(headerName);
        logger.info("Validating header - Name: {}, Expected: {}, Actual: {}",
            headerName, expectedValue, actualValue);

        assertThat(actualValue)
            .as("Header validation for: " + headerName)
            .isEqualTo(expectedValue);

        return this;
    }

    @Step("Validate content type is {expectedContentType}")
    public ResponseValidator validateContentType(String expectedContentType) {
        String actualContentType = response.getContentType();
        logger.info("Validating content type - Expected: {}, Actual: {}",
            expectedContentType, actualContentType);

        assertThat(actualContentType)
            .as("Content type validation")
            .contains(expectedContentType);

        return this;
    }

    @Step("Validate JSON schema: {schemaFileName}")
    public ResponseValidator validateSchema(String schemaFileName) {
        logger.info("Validating JSON schema: {}", schemaFileName);

        try {
            response.then().assertThat().body(SchemaValidator.getSchemaValidator(schemaFileName));
            logger.info("Schema validation successful");
        } catch (AssertionError e) {
            logger.error("Schema validation failed: {}", e.getMessage());
            throw e;
        }

        return this;
    }

    @Step("Validate complete JSON body")
    public ResponseValidator validateCompleteBody(String expectedJson) {
        String actualJson = response.getBody().asString();
        logger.info("Validating complete JSON body");

        JsonBodyValidator.assertJsonEqualsLenient(expectedJson, actualJson);

        return this;
    }

    @Step("Validate complete JSON body (strict mode)")
    public ResponseValidator validateCompleteBodyStrict(String expectedJson) {
        String actualJson = response.getBody().asString();
        logger.info("Validating complete JSON body (strict mode)");

        JsonBodyValidator.assertJsonEqualsStrict(expectedJson, actualJson);

        return this;
    }

    @Step("Validate JSON body ignoring fields: {fieldsToIgnore}")
    public ResponseValidator validateBodyIgnoringFields(String expectedJson, String... fieldsToIgnore) {
        String actualJson = response.getBody().asString();
        logger.info("Validating JSON body ignoring fields: {}", String.join(", ", fieldsToIgnore));

        JsonBodyValidator.assertJsonEqualsIgnoringFields(expectedJson, actualJson, fieldsToIgnore);

        return this;
    }

    @Step("Validate JSONPath {jsonPath}")
    public ResponseValidator validateJsonPath(String jsonPath, Matcher<?> matcher) {
        logger.info("Validating JSONPath: {}", jsonPath);

        response.then().assertThat().body(jsonPath, matcher);

        return this;
    }

    @Step("Validate field {fieldPath} equals {expectedValue}")
    public ResponseValidator validateField(String fieldPath, Object expectedValue) {
        Object actualValue = response.jsonPath().get(fieldPath);
        logger.info("Validating field - Path: {}, Expected: {}, Actual: {}",
            fieldPath, expectedValue, actualValue);

        assertThat(actualValue)
            .as("Field validation for: " + fieldPath)
            .isEqualTo(expectedValue);

        return this;
    }

    @Step("Validate field {fieldPath} is not null")
    public ResponseValidator validateFieldNotNull(String fieldPath) {
        Object actualValue = response.jsonPath().get(fieldPath);
        logger.info("Validating field is not null: {}", fieldPath);

        assertThat(actualValue)
            .as("Field not null validation for: " + fieldPath)
            .isNotNull();

        return this;
    }

    @Step("Validate response body is not empty")
    public ResponseValidator validateBodyNotEmpty() {
        String body = response.getBody().asString();
        logger.info("Validating response body is not empty");

        assertThat(body)
            .as("Response body not empty validation")
            .isNotEmpty();

        return this;
    }

    public Response getResponse() {
        return response;
    }
}
