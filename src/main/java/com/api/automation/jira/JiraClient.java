package com.api.automation.jira;

import com.api.automation.config.EnvironmentConfig;
import com.api.automation.constants.Constants;
import com.api.automation.constants.FailureCategory;
import com.api.automation.exceptions.JiraIntegrationException;
import com.api.automation.logging.MaskingUtils;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JiraClient {

    private static final Logger logger = LoggerFactory.getLogger(JiraClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final String jiraBaseUrl;
    private final String jiraProjectKey;
    private final String authHeader;

    public JiraClient(EnvironmentConfig config) {
        this.jiraBaseUrl = config.getJiraBaseUrl();
        this.jiraProjectKey = config.getJiraProjectKey();

        String credentials = Credentials.basic(config.getJiraUsername(), config.getJiraApiToken());
        this.authHeader = credentials;

        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    public String createIssue(String summary, String description, FailureCategory category, String signature) {
        try {
            JSONObject issuePayload = buildCreateIssuePayload(summary, description, category, signature);

            RequestBody body = RequestBody.create(issuePayload.toString(), JSON);
            Request request = new Request.Builder()
                .url(jiraBaseUrl + "/rest/api/2/issue")
                .header("Authorization", authHeader)
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    logger.error("Failed to create JIRA issue. Status: {}, Body: {}", response.code(), errorBody);
                    throw new JiraIntegrationException("Failed to create JIRA issue: " + response.code());
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                String issueKey = jsonResponse.getString("key");

                logger.info("JIRA issue created successfully: {}", issueKey);
                return issueKey;
            }
        } catch (IOException | org.json.JSONException e) {
            logger.error("Error creating JIRA issue", e);
            throw new JiraIntegrationException("Error creating JIRA issue", e);
        }
    }

    public List<String> searchIssueBySignature(String signature) {
        try {
            String jql = String.format("project = %s AND %s ~ \"%s\" AND status != Closed",
                jiraProjectKey, Constants.JIRA_CUSTOM_FIELD_SIGNATURE, signature);

            String encodedJql = java.net.URLEncoder.encode(jql, "UTF-8");
            String url = jiraBaseUrl + "/rest/api/2/search?jql=" + encodedJql + "&fields=key,summary,status";

            Request request = new Request.Builder()
                .url(url)
                .header("Authorization", authHeader)
                .get()
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.warn("Failed to search JIRA issues. Status: {}", response.code());
                    return new ArrayList<>();
                }

                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONArray issues = jsonResponse.getJSONArray("issues");

                List<String> issueKeys = new ArrayList<>();
                for (int i = 0; i < issues.length(); i++) {
                    String key = issues.getJSONObject(i).getString("key");
                    issueKeys.add(key);
                }

                logger.info("Found {} existing issues with signature: {}", issueKeys.size(), signature);
                return issueKeys;
            }
        } catch (IOException | org.json.JSONException e) {
            logger.error("Error searching JIRA issues", e);
            return new ArrayList<>();
        }
    }

    public void updateIssue(String issueKey, String comment) {
        try {
            JSONObject commentPayload = new JSONObject();
            commentPayload.put("body", comment);

            RequestBody body = RequestBody.create(commentPayload.toString(), JSON);
            Request request = new Request.Builder()
                .url(jiraBaseUrl + "/rest/api/2/issue/" + issueKey + "/comment")
                .header("Authorization", authHeader)
                .post(body)
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.warn("Failed to update JIRA issue {}. Status: {}", issueKey, response.code());
                } else {
                    logger.info("JIRA issue {} updated successfully", issueKey);
                }
            }
        } catch (IOException | org.json.JSONException e) {
            logger.error("Error updating JIRA issue: {}", issueKey, e);
        }
    }

    private JSONObject buildCreateIssuePayload(String summary, String description, FailureCategory category, String signature) {
        JSONObject payload = new JSONObject();

        JSONObject fields = new JSONObject();
        fields.put("summary", summary);
        fields.put("description", description);

        JSONObject project = new JSONObject();
        project.put("key", jiraProjectKey);
        fields.put("project", project);

        JSONObject issueType = new JSONObject();
        issueType.put("name", "Bug");
        fields.put("issuetype", issueType);

        // Add custom fields
        fields.put(Constants.JIRA_CUSTOM_FIELD_SIGNATURE, signature);
        fields.put(Constants.JIRA_CUSTOM_FIELD_ENVIRONMENT, EnvironmentConfig.load().getEnvironment());

        // Add labels
        JSONArray labels = new JSONArray();
        labels.put(Constants.JIRA_LABEL_AUTOMATION);
        labels.put(category.name());
        fields.put("labels", labels);

        payload.put("fields", fields);

        return payload;
    }

    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
        }
    }
}
