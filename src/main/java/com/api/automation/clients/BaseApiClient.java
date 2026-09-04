package com.api.automation.clients;

import com.api.automation.base.SpecificationFactory;
import com.api.automation.context.TestContext;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static io.restassured.RestAssured.given;

public abstract class BaseApiClient {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected RequestSpecification getRequestSpec() {
        return SpecificationFactory.getBaseRequestSpec();
    }

    protected RequestSpecification getAuthenticatedRequestSpec(String token) {
        return SpecificationFactory.getAuthenticatedRequestSpec(token);
    }

    @Step("GET {endpoint}")
    protected Response get(String endpoint) {
        logger.info("GET request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .when()
            .get(endpoint);
    }

    @Step("GET {endpoint} with path params")
    protected Response get(String endpoint, Map<String, Object> pathParams) {
        logger.info("GET request to: {} with path params: {}", endpoint, pathParams);
        return given()
            .spec(getRequestSpec())
            .pathParams(pathParams)
            .when()
            .get(endpoint);
    }

    @Step("GET {endpoint} with query params")
    protected Response getWithQueryParams(String endpoint, Map<String, Object> queryParams) {
        logger.info("GET request to: {} with query params: {}", endpoint, queryParams);
        return given()
            .spec(getRequestSpec())
            .queryParams(queryParams)
            .when()
            .get(endpoint);
    }

    @Step("POST {endpoint}")
    protected Response post(String endpoint, Object body) {
        logger.info("POST request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .body(body)
            .when()
            .post(endpoint);
    }

    @Step("POST {endpoint} with path params")
    protected Response post(String endpoint, Object body, Map<String, Object> pathParams) {
        logger.info("POST request to: {} with path params: {}", endpoint, pathParams);
        return given()
            .spec(getRequestSpec())
            .pathParams(pathParams)
            .body(body)
            .when()
            .post(endpoint);
    }

    @Step("PUT {endpoint}")
    protected Response put(String endpoint, Object body) {
        logger.info("PUT request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .body(body)
            .when()
            .put(endpoint);
    }

    @Step("PUT {endpoint} with path params")
    protected Response put(String endpoint, Object body, Map<String, Object> pathParams) {
        logger.info("PUT request to: {} with path params: {}", endpoint, pathParams);
        return given()
            .spec(getRequestSpec())
            .pathParams(pathParams)
            .body(body)
            .when()
            .put(endpoint);
    }

    @Step("PATCH {endpoint}")
    protected Response patch(String endpoint, Object body) {
        logger.info("PATCH request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .body(body)
            .when()
            .patch(endpoint);
    }

    @Step("PATCH {endpoint} with path params")
    protected Response patch(String endpoint, Object body, Map<String, Object> pathParams) {
        logger.info("PATCH request to: {} with path params: {}", endpoint, pathParams);
        return given()
            .spec(getRequestSpec())
            .pathParams(pathParams)
            .body(body)
            .when()
            .patch(endpoint);
    }

    @Step("DELETE {endpoint}")
    protected Response delete(String endpoint) {
        logger.info("DELETE request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .when()
            .delete(endpoint);
    }

    @Step("DELETE {endpoint} with path params")
    protected Response delete(String endpoint, Map<String, Object> pathParams) {
        logger.info("DELETE request to: {} with path params: {}", endpoint, pathParams);
        return given()
            .spec(getRequestSpec())
            .pathParams(pathParams)
            .when()
            .delete(endpoint);
    }

    @Step("HEAD {endpoint}")
    protected Response head(String endpoint) {
        logger.info("HEAD request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .when()
            .head(endpoint);
    }

    @Step("OPTIONS {endpoint}")
    protected Response options(String endpoint) {
        logger.info("OPTIONS request to: {}", endpoint);
        return given()
            .spec(getRequestSpec())
            .when()
            .options(endpoint);
    }

    protected void storeInContext(String key, Object value) {
        TestContext.get().setTestData(key, value);
    }

    protected Object getFromContext(String key) {
        return TestContext.get().getTestData(key);
    }
}
