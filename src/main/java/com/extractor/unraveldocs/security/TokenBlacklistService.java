package com.extractor.unraveldocs.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:accesstoken:";

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String tokenJti, long expiresInSeconds) {
        // Store the JTI with its original expiry duration
        // This ensures Redis automatically cleans up expired blacklisted tokens
        redisTemplate.opsForValue().set(BLACKLIST_PREFIX + tokenJti, "blacklisted", expiresInSeconds, TimeUnit.SECONDS);
    }

    public boolean isTokenBlacklisted(String tokenJti) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + tokenJti);
    }
}