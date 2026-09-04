package com.api.automation.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = createObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("JsonUtils class cannot be instantiated");
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        return mapper;
    }

    public static <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to {}: {}", clazz.getSimpleName(), json, e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public static <T> List<T> deserializeList(String json, Class<T> elementClass) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize JSON to List<{}>: {}", elementClass.getSimpleName(), json, e);
            throw new RuntimeException("Deserialization failed", e);
        }
    }

    public static String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object: {}", object, e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    public static String serializePretty(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize object: {}", object, e);
            throw new RuntimeException("Serialization failed", e);
        }
    }

    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse JSON: {}", json, e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    public static Map<String, Object> jsonToMap(String json) {
        try {
            return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert JSON to Map: {}", json, e);
            throw new RuntimeException("JSON to Map conversion failed", e);
        }
    }

    public static String readJsonFromFile(String filePath) {
        try {
            return new String(JsonUtils.class.getClassLoader()
                .getResourceAsStream(filePath)
                .readAllBytes());
        } catch (IOException e) {
            logger.error("Failed to read JSON from file: {}", filePath, e);
            throw new RuntimeException("Failed to read JSON file", e);
        }
    }

    public static <T> T readObjectFromFile(String filePath, Class<T> clazz) {
        String json = readJsonFromFile(filePath);
        return deserialize(json, clazz);
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
