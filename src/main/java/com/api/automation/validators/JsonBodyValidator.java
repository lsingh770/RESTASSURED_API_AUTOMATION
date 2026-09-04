package com.api.automation.validators;

import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class JsonBodyValidator {

    private static final Logger logger = LoggerFactory.getLogger(JsonBodyValidator.class);

    private JsonBodyValidator() {
        throw new UnsupportedOperationException("JsonBodyValidator class cannot be instantiated");
    }

    public static void assertJsonEquals(String expectedJson, String actualJson) {
        assertJsonEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    public static void assertJsonEquals(String expectedJson, String actualJson, JSONCompareMode compareMode) {
        try {
            logger.debug("Comparing JSON with mode: {}", compareMode);
            JSONAssert.assertEquals(expectedJson, actualJson, compareMode);
            logger.info("JSON comparison successful");
        } catch (JSONException e) {
            logger.error("JSON comparison failed: {}", e.getMessage());
            throw new AssertionError("JSON comparison failed: " + e.getMessage(), e);
        }
    }

    public static void assertJsonEqualsIgnoringFields(String expectedJson, String actualJson, String... fieldsToIgnore) {
        try {
            JSONComparator comparator = createComparatorIgnoringFields(fieldsToIgnore);
            JSONAssert.assertEquals(expectedJson, actualJson, comparator);
            logger.info("JSON comparison successful (ignored fields: {})", String.join(", ", fieldsToIgnore));
        } catch (JSONException e) {
            logger.error("JSON comparison failed: {}", e.getMessage());
            throw new AssertionError("JSON comparison failed: " + e.getMessage(), e);
        }
    }

    public static void assertJsonEqualsLenient(String expectedJson, String actualJson) {
        assertJsonEquals(expectedJson, actualJson, JSONCompareMode.LENIENT);
    }

    public static void assertJsonEqualsStrict(String expectedJson, String actualJson) {
        assertJsonEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    public static void assertJsonEqualsIgnoringOrder(String expectedJson, String actualJson) {
        assertJsonEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private static JSONComparator createComparatorIgnoringFields(String... fieldsToIgnore) {
        List<org.skyscreamer.jsonassert.Customization> customizations = new ArrayList<>();

        for (String field : fieldsToIgnore) {
            // Support both simple field names and nested paths (e.g., "**.timestamp", "user.id")
            String path = field.startsWith("**") ? field : "**." + field;
            customizations.add(new org.skyscreamer.jsonassert.Customization(path, (o1, o2) -> true));
        }

        return new CustomComparator(
            JSONCompareMode.LENIENT,
            customizations.toArray(new org.skyscreamer.jsonassert.Customization[0])
        );
    }

    public static void assertPartialJsonMatch(String expectedPartialJson, String actualJson) {
        assertJsonEquals(expectedPartialJson, actualJson, JSONCompareMode.LENIENT);
    }
}
