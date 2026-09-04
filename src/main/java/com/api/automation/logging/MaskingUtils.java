package com.api.automation.logging;

import com.api.automation.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class MaskingUtils {

    private static final Logger logger = LoggerFactory.getLogger(MaskingUtils.class);
    private static final String MASK_PATTERN = "********";
    private static final Pattern SENSITIVE_PATTERN = buildSensitivePattern();

    private MaskingUtils() {
        throw new UnsupportedOperationException("MaskingUtils class cannot be instantiated");
    }

    private static Pattern buildSensitivePattern() {
        StringBuilder regex = new StringBuilder("(?i)(");
        for (int i = 0; i < Constants.SENSITIVE_FIELDS.length; i++) {
            if (i > 0) {
                regex.append("|");
            }
            regex.append(Constants.SENSITIVE_FIELDS[i]);
        }
        regex.append(")([\"']?\\s*[:=]\\s*[\"']?)([^,\\}\\]\\s\"']+)");
        return Pattern.compile(regex.toString());
    }

    public static String maskSensitiveData(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        try {
            return SENSITIVE_PATTERN.matcher(input).replaceAll("$1$2" + MASK_PATTERN);
        } catch (Exception e) {
            logger.warn("Error masking sensitive data", e);
            return input;
        }
    }

    public static String maskAuthorizationHeader(String headerValue) {
        if (headerValue == null || headerValue.isEmpty()) {
            return headerValue;
        }

        if (headerValue.toLowerCase().startsWith("bearer ")) {
            return "Bearer " + MASK_PATTERN;
        } else if (headerValue.toLowerCase().startsWith("basic ")) {
            return "Basic " + MASK_PATTERN;
        } else {
            return MASK_PATTERN;
        }
    }

    public static String maskJsonField(String json, String fieldName) {
        if (json == null || json.isEmpty() || fieldName == null) {
            return json;
        }

        String pattern = String.format("(\"%s\"\\s*:\\s*\")([^\"]+)(\")", fieldName);
        return json.replaceAll(pattern, "$1" + MASK_PATTERN + "$3");
    }
}
