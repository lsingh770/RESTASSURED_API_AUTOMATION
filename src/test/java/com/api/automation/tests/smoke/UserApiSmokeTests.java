package com.api.automation.tests.smoke;

import com.api.automation.annotations.ApiTest;
import com.api.automation.assertions.ApiAssertions;
import com.api.automation.clients.UserApiClient;
import com.api.automation.models.request.CreateUserRequest;
import com.api.automation.models.request.UpdateUserRequest;
import com.api.automation.models.response.UserResponse;
import com.api.automation.retry.RetryAnalyzer;
import com.api.automation.utilities.JsonUtils;
import com.api.automation.utilities.RandomDataGenerator;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("User Management")
@Feature("User API")
public class UserApiSmokeTests {

    private UserApiClient userApi;

    @BeforeClass
    public void setup() {
        userApi = new UserApiClient();
    }

    @Test(groups = {"smoke"}, priority = 1, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-001", endpoint = "POST /users", category = "SMOKE")
    @Story("Create User")
    @Description("Verify that a new user can be created successfully with valid data")
    public void shouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequest request = RandomDataGenerator.createValidUser();

        // Act
        Response response = userApi.createUser(request);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(201)
            .validateSchema("user-response-schema.json")
            .validateFieldNotNull("id")
            .validateField("email", request.getEmail())
            .validateField("username", request.getUsername())
            .validateResponseTime(2000);

        // Extract and validate user ID
        UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.getId()).isNotNull();
        assertThat(userResponse.getEmail()).isEqualTo(request.getEmail());
    }

    @Test(groups = {"smoke"}, priority = 2, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-002", endpoint = "GET /users/{id}", category = "SMOKE")
    @Story("Get User")
    @Description("Verify that an existing user can be retrieved by ID")
    public void shouldGetUserByIdSuccessfully() {
        // Arrange - Create a user first
        CreateUserRequest createRequest = RandomDataGenerator.createValidUser();
        Response createResponse = userApi.createUser(createRequest);
        String userId = createResponse.jsonPath().getString("id");

        // Act
        Response response = userApi.getUserById(userId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(200)
            .validateSchema("user-response-schema.json")
            .validateField("id", userId)
            .validateField("email", createRequest.getEmail())
            .validateResponseTime(2000);
    }

    @Test(groups = {"smoke"}, priority = 3, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-003", endpoint = "PUT /users/{id}", category = "SMOKE")
    @Story("Update User")
    @Description("Verify that an existing user can be updated successfully")
    public void shouldUpdateUserSuccessfully() {
        // Arrange - Create a user first
        CreateUserRequest createRequest = RandomDataGenerator.createValidUser();
        Response createResponse = userApi.createUser(createRequest);
        String userId = createResponse.jsonPath().getString("id");

        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
            .firstName("Updated")
            .lastName("User")
            .email(RandomDataGenerator.generateUniqueEmail())
            .build();

        // Act
        Response response = userApi.updateUser(userId, updateRequest);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(200)
            .validateField("id", userId)
            .validateField("firstName", "Updated")
            .validateField("lastName", "User")
            .validateResponseTime(2000);
    }

    @Test(groups = {"smoke"}, priority = 4, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-004", endpoint = "DELETE /users/{id}", category = "SMOKE")
    @Story("Delete User")
    @Description("Verify that an existing user can be deleted successfully")
    public void shouldDeleteUserSuccessfully() {
        // Arrange - Create a user first
        CreateUserRequest createRequest = RandomDataGenerator.createValidUser();
        Response createResponse = userApi.createUser(createRequest);
        String userId = createResponse.jsonPath().getString("id");

        // Act
        Response response = userApi.deleteUser(userId);

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(204);

        // Verify user is deleted
        Response getResponse = userApi.getUserById(userId);
        ApiAssertions.assertThat(getResponse)
            .validateStatusCode(404);
    }

    @Test(groups = {"smoke"}, priority = 5, retryAnalyzer = RetryAnalyzer.class)
    @ApiTest(requirement = "USER-005", endpoint = "GET /users", category = "SMOKE")
    @Story("Get All Users")
    @Description("Verify that all users can be retrieved")
    public void shouldGetAllUsersSuccessfully() {
        // Act
        Response response = userApi.getAllUsers();

        // Assert
        ApiAssertions.assertThat(response)
            .validateStatusCode(200)
            .validateBodyNotEmpty()
            .validateResponseTime(2000);
    }
}
