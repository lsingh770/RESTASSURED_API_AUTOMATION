package com.api.automation.context;

import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestContext {

    private static final ThreadLocal<TestContext> context = ThreadLocal.withInitial(TestContext::new);

    private final String correlationId;
    private final String requestId;
    private final Map<String, Object> testData;
    private RequestSpecification requestSpecification;
    private long testStartTime;

    private TestContext() {
        this.correlationId = UUID.randomUUID().toString();
        this.requestId = UUID.randomUUID().toString();
        this.testData = new HashMap<>();
        this.testStartTime = System.currentTimeMillis();
    }

    public static TestContext get() {
        return context.get();
    }

    public static void reset() {
        context.remove();
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestSpecification getRequestSpecification() {
        return requestSpecification;
    }

    public void setRequestSpecification(RequestSpecification requestSpecification) {
        this.requestSpecification = requestSpecification;
    }

    public void setTestData(String key, Object value) {
        testData.put(key, value);
    }

    public Object getTestData(String key) {
        return testData.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getTestData(String key, Class<T> type) {
        return (T) testData.get(key);
    }

    public Map<String, Object> getAllTestData() {
        return new HashMap<>(testData);
    }

    public void clearTestData() {
        testData.clear();
    }

    public long getTestStartTime() {
        return testStartTime;
    }

    public long getTestDuration() {
        return System.currentTimeMillis() - testStartTime;
    }
}
