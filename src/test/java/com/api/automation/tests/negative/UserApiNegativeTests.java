package com.api.automation.tests.negative;

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
@Feature("User API - Negative Scenarios")
public class UserApiNegativeTests {

    private UserApiClient userApi;

    @BeforeClass
    public void setup() {
        userApi = new UserApiClient();
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-001", endpoint = "POST /users", category = "NEGATIVE")
    @Story("Create User with Missing Required Fields")
    @Description("Verify that creating a user with missing required fields returns 400 Bad Request")
    public void shouldFailToCreateUserWithMissingRequiredFields() {
        // Arrange - Create request with missing required fields
        CreateUserRequest request = CreateUserRequest.builder()
            .firstName("Test")
            // Missing email, username, etc.
            .build();

        // Act
        Response response = userApi.createUser(request);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(400)
            .validateSchema("error-response-schema.json")
            .validateFieldNotNull("message");
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-002", endpoint = "POST /users", category = "NEGATIVE")
    @Story("Create User with Invalid Email")
    @Description("Verify that creating a user with invalid email format returns 400 Bad Request")
    public void shouldFailToCreateUserWithInvalidEmail() {
        // Arrange
        CreateUserRequest request = RandomDataGenerator.createValidUser();
        request.setEmail("invalid-email-format");

        // Act
        Response response = userApi.createUser(request);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(400)
            .validateSchema("error-response-schema.json");
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-003", endpoint = "POST /users", category = "NEGATIVE")
    @Story("Create Duplicate User")
    @Description("Verify that creating a user with duplicate email returns 409 Conflict")
    public void shouldFailToCreateDuplicateUser() {
        // Arrange - Create a user first
        CreateUserRequest request = RandomDataGenerator.createValidUser();
        userApi.createUser(request);

        // Act - Try to create same user again
        Response response = userApi.createUser(request);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(409)
            .validateSchema("error-response-schema.json");
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-004", endpoint = "GET /users/{id}", category = "NEGATIVE")
    @Story("Get Non-Existent User")
    @Description("Verify that getting a non-existent user returns 404 Not Found")
    public void shouldFailToGetNonExistentUser() {
        // Arrange
        String nonExistentUserId = "non-existent-id-12345";

        // Act
        Response response = userApi.getUserById(nonExistentUserId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(404)
            .validateSchema("error-response-schema.json");
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-005", endpoint = "PUT /users/{id}", category = "NEGATIVE")
    @Story("Update Non-Existent User")
    @Description("Verify that updating a non-existent user returns 404 Not Found")
    public void shouldFailToUpdateNonExistentUser() {
        // Arrange
        String nonExistentUserId = "non-existent-id-12345";
        CreateUserRequest updateRequest = RandomDataGenerator.createValidUser();

        // Act
        Response response = userApi.updateUser(nonExistentUserId,
            com.api.automation.models.request.UpdateUserRequest.builder()
                .firstName(updateRequest.getFirstName())
                .build());

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(404)
            .validateSchema("error-response-schema.json");
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-006", endpoint = "DELETE /users/{id}", category = "NEGATIVE")
    @Story("Delete Non-Existent User")
    @Description("Verify that deleting a non-existent user returns 404 Not Found")
    public void shouldFailToDeleteNonExistentUser() {
        // Arrange
        String nonExistentUserId = "non-existent-id-12345";

        // Act
        Response response = userApi.deleteUser(nonExistentUserId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(404);
    }

    @Test(groups = {"negative", "regression"}, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-NEG-007", endpoint = "GET /users/{id}", category = "NEGATIVE")
    @Story("Get User with Invalid ID Format")
    @Description("Verify that getting a user with invalid ID format returns 400 Bad Request")
    public void shouldFailToGetUserWithInvalidIdFormat() {
        // Arrange
        String invalidUserId = "@@invalid-id@@";

        // Act
        Response response = userApi.getUserById(invalidUserId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCodeInRange(400, 404);
    }
}
