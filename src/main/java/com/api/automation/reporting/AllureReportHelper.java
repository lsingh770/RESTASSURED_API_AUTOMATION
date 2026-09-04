package com.api.automation.reporting;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class AllureReportHelper {

    private AllureReportHelper() {
        throw new UnsupportedOperationException("AllureReportHelper class cannot be instantiated");
    }

    @Step("{stepDescription}")
    public static void step(String stepDescription) {
        // Step is automatically captured by @Step annotation
    }

    public static void addParameter(String name, Object value) {
        Allure.parameter(name, value);
    }

    public static void addTextAttachment(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, "txt");
    }

    public static void addJsonAttachment(String name, String jsonContent) {
        Allure.addAttachment(name, "application/json", jsonContent, "json");
    }

    public static void addXmlAttachment(String name, String xmlContent) {
        Allure.addAttachment(name, "application/xml", xmlContent, "xml");
    }

    public static void addHtmlAttachment(String name, String htmlContent) {
        Allure.addAttachment(name, "text/html", htmlContent, "html");
    }

    public static void addByteAttachment(String name, String type, byte[] content) {
        Allure.addAttachment(name, type, new ByteArrayInputStream(content), "");
    }

    public static void addLink(String name, String url) {
        Allure.link(name, url);
    }

    public static void addIssueLink(String issueKey) {
        Allure.issue(issueKey, issueKey);
    }

    public static void addTestCaseLink(String testCaseId) {
        Allure.tms(testCaseId, testCaseId);
    }

    public static void addLabel(String name, String value) {
        Allure.label(name, value);
    }

    public static void addEpic(String epic) {
        Allure.epic(epic);
    }

    public static void addFeature(String feature) {
        Allure.feature(feature);
    }

    public static void addStory(String story) {
        Allure.story(story);
    }

    public static void addSeverity(io.qameta.allure.SeverityLevel severity) {
        Allure.label("severity", severity.value());
    }

    public static void addEnvironment(String key, String value) {
        Allure.parameter(key, value);
    }
}
