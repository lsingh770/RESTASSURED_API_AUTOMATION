package com.api.automation.validators;

import com.api.automation.constants.Constants;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public class SchemaValidator {

    private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);

    private SchemaValidator() {
        throw new UnsupportedOperationException("SchemaValidator class cannot be instantiated");
    }

    public static JsonSchemaValidator getSchemaValidator(String schemaFileName) {
        String schemaPath = Constants.SCHEMA_BASE_PATH + schemaFileName;
        logger.debug("Loading schema from: {}", schemaPath);

        InputStream schemaStream = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
        if (schemaStream == null) {
            throw new IllegalArgumentException("Schema file not found: " + schemaPath);
        }

        return JsonSchemaValidator.matchesJsonSchema(schemaStream);
    }

    public static JsonSchemaValidator getSchemaValidatorFromString(String schemaJson) {
        return JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaJson);
    }
}
