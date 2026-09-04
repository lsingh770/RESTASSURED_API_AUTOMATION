package com.api.automation.config;

import com.api.automation.constants.Constants;
import com.api.automation.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static ConfigManager instance;
    private final Properties properties;
    private final String environment;

    private ConfigManager() {
        this.environment = determineEnvironment();
        this.properties = loadConfiguration();
        logger.info("Configuration loaded for environment: {}", environment);
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private String determineEnvironment() {
        String env = System.getProperty(Constants.ENV_KEY);
        if (env == null || env.trim().isEmpty()) {
            env = System.getenv("ENV");
        }
        if (env == null || env.trim().isEmpty()) {
            env = Constants.DEFAULT_ENV;
            logger.warn("No environment specified. Using default: {}", env);
        }
        return env.toLowerCase();
    }

    private Properties loadConfiguration() {
        Properties props = new Properties();

        // Load application.properties first (base configuration)
        loadPropertiesFile(props, "config/application.properties");

        // Load environment-specific properties (overrides base)
        String envConfigFile = "config/" + environment + ".properties";
        loadPropertiesFile(props, envConfigFile);

        // System properties override everything
        props.putAll(System.getProperties());

        return props;
    }

    private void loadPropertiesFile(Properties props, String fileName) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                logger.warn("Configuration file not found: {}", fileName);
                return;
            }
            props.load(input);
            logger.info("Loaded configuration from: {}", fileName);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file: " + fileName, e);
        }
    }

    public String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property not found: {}", key);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for property {}: {}. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public long getLongProperty(String key, long defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid long value for property {}: {}. Using default: {}", key, value, defaultValue);
            return defaultValue;
        }
    }

    public String getEnvironment() {
        return environment;
    }

    public Properties getAllProperties() {
        return new Properties(properties);
    }
}
