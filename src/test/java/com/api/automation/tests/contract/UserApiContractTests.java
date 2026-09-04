package com.api.automation.tests.contract;

import com.api.automation.annotations.ApiTest;
import com.api.automation.assertions.ApiAssertions;
import com.api.automation.clients.UserApiClient;
import com.api.automation.models.request.CreateUserRequest;
import com.api.automation.retry.RetryAnalyzer;
import com.api.automation.utilities.RandomDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Epic("User Management")
@Feature("User API - Contract Validation")
public class UserApiContractTests {

    private UserApiClient userApi;

    @BeforeClass
    public void setup() {
        userApi = new UserApiClient();
    }

    @Test(groups = {"contract", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-CONTRACT-001", endpoint = "POST /users", category = "CONTRACT")
    @Story("Validate Create User Response Schema")
    @Description("Verify that the create user response matches the defined JSON schema")
    public void shouldValidateCreateUserResponseSchema() {
        // Arrange
        CreateUserRequest request = RandomDataGenerator.createValidUser();

        // Act
        Response response = userApi.createUser(request);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(201)
            .validateSchema("user-response-schema.json");
    }

    @Test(groups = {"contract", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-CONTRACT-002", endpoint = "GET /users/{id}", category = "CONTRACT")
    @Story("Validate Get User Response Schema")
    @Description("Verify that the get user response matches the defined JSON schema")
    public void shouldValidateGetUserResponseSchema() {
        // Arrange - Create a user first
        CreateUserRequest createRequest = RandomDataGenerator.createValidUser();
        Response createResponse = userApi.createUser(createRequest);
        String userId = createResponse.jsonPath().getString("id");

        // Act
        Response response = userApi.getUserById(userId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(200)
            .validateSchema("user-response-schema.json");
    }

    @Test(groups = {"contract", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-CONTRACT-003", endpoint = "GET /users/{id}", category = "CONTRACT")
    @Story("Validate Error Response Schema")
    @Description("Verify that error responses match the defined error schema")
    public void shouldValidateErrorResponseSchema() {
        // Arrange
        String nonExistentUserId = "non-existent-id-12345";

        // Act
        Response response = userApi.getUserById(nonExistentUserId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(404)
            .validateSchema("error-response-schema.json");
    }
}
