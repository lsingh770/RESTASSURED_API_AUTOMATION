package com.api.automation.clients;

import com.api.automation.constants.ApiEndpoints;
import com.api.automation.models.request.CreateUserRequest;
import com.api.automation.models.request.UpdateUserRequest;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class UserApiClient extends BaseApiClient {

    @Step("Create user")
    public Response createUser(CreateUserRequest request) {
        logger.info("Creating user with email: {}", request.getEmail());
        return post(ApiEndpoints.USERS, request);
    }

    @Step("Get user by ID: {userId}")
    public Response getUserById(String userId) {
        logger.info("Getting user by ID: {}", userId);
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        return get(ApiEndpoints.USER_BY_ID, pathParams);
    }

    @Step("Update user: {userId}")
    public Response updateUser(String userId, UpdateUserRequest request) {
        logger.info("Updating user: {}", userId);
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        return put(ApiEndpoints.USER_BY_ID, request, pathParams);
    }

    @Step("Patch user: {userId}")
    public Response patchUser(String userId, UpdateUserRequest request) {
        logger.info("Patching user: {}", userId);
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        return patch(ApiEndpoints.USER_BY_ID, request, pathParams);
    }

    @Step("Delete user: {userId}")
    public Response deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);
        Map<String, Object> pathParams = new HashMap<>();
        pathParams.put("id", userId);
        return delete(ApiEndpoints.USER_BY_ID, pathParams);
    }

    @Step("Get all users")
    public Response getAllUsers() {
        logger.info("Getting all users");
        return get(ApiEndpoints.USERS);
    }

    @Step("Search users with query params")
    public Response searchUsers(Map<String, Object> queryParams) {
        logger.info("Searching users with params: {}", queryParams);
        return getWithQueryParams(ApiEndpoints.USER_SEARCH, queryParams);
    }

    @Step("Get users with pagination")
    public Response getUsersWithPagination(int page, int pageSize) {
        logger.info("Getting users - Page: {}, Size: {}", page, pageSize);
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", page);
        queryParams.put("pageSize", pageSize);
        return getWithQueryParams(ApiEndpoints.USERS, queryParams);
    }
}
