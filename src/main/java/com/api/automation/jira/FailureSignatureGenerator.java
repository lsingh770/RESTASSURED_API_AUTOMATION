package com.api.automation.jira;

import com.api.automation.constants.FailureCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FailureSignatureGenerator {

    private static final Logger logger = LoggerFactory.getLogger(FailureSignatureGenerator.class);

    private FailureSignatureGenerator() {
        throw new UnsupportedOperationException("FailureSignatureGenerator class cannot be instantiated");
    }

    public static String generateSignature(String testName, String endpoint, FailureCategory category, String errorMessage) {
        String signatureInput = buildSignatureInput(testName, endpoint, category, errorMessage);
        return generateSHA256Hash(signatureInput);
    }

    private static String buildSignatureInput(String testName, String endpoint, FailureCategory category, String errorMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append(normalize(testName));
        builder.append("|");
        builder.append(normalize(endpoint));
        builder.append("|");
        builder.append(category.name());
        builder.append("|");
        builder.append(normalize(extractErrorSignature(errorMessage)));
        return builder.toString();
    }

    private static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toLowerCase();
    }

    private static String extractErrorSignature(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "";
        }

        // Extract first 100 chars to avoid signature changes due to dynamic data
        String signature = errorMessage.length() > 100 ? errorMessage.substring(0, 100) : errorMessage;

        // Remove dynamic data like timestamps, IDs, etc.
        signature = signature.replaceAll("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}", "TIMESTAMP");
        signature = signature.replaceAll("\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b", "UUID");
        signature = signature.replaceAll("\\d+", "NUM");

        return signature;
    }

    private static String generateSHA256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            // Fallback to simple hash if SHA-256 not available
            return String.valueOf(input.hashCode());
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
