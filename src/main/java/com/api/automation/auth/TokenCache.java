package com.api.automation.auth;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class TokenCache {

    private static final ConcurrentHashMap<String, CachedToken> tokenCache = new ConcurrentHashMap<>();

    private TokenCache() {
        throw new UnsupportedOperationException("TokenCache class cannot be instantiated");
    }

    public static void put(String key, String token, long expiryTimestamp) {
        tokenCache.put(key, new CachedToken(token, expiryTimestamp));
    }

    public static String get(String key) {
        CachedToken cachedToken = tokenCache.get(key);
        if (cachedToken == null) {
            return null;
        }

        if (cachedToken.isExpired()) {
            tokenCache.remove(key);
            return null;
        }

        return cachedToken.getToken();
    }

    public static void invalidate(String key) {
        tokenCache.remove(key);
    }

    public static void clear() {
        tokenCache.clear();
    }

    public static boolean contains(String key) {
        CachedToken cachedToken = tokenCache.get(key);
        return cachedToken != null && !cachedToken.isExpired();
    }

    private static class CachedToken {
        private final String token;
        private final long expiryTimestamp;

        CachedToken(String token, long expiryTimestamp) {
            this.token = token;
            this.expiryTimestamp = expiryTimestamp;
        }

        String getToken() {
            return token;
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() >= expiryTimestamp;
        }
    }
}
