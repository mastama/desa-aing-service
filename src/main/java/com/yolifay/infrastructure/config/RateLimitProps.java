package com.yolifay.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limit.login")
public record RateLimitProps(
        int maxAttempts,
        int windowSeconds
) {
}
