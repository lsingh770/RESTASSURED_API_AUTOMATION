package com.api.automation.assertions;

import com.api.automation.validators.ResponseValidator;
import io.restassured.response.Response;

public class ApiAssertions {

    private ApiAssertions() {
        throw new UnsupportedOperationException("ApiAssertions class cannot be instantiated");
    }

    public static ResponseValidator assertThat(Response response) {
        return new ResponseValidator(response);
    }
}
