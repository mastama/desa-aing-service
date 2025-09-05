package com.yolifay.infrastructure.adapter.out.cache;

import com.yolifay.domain.port.out.RateLimiterPortOut;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiterAdapter implements RateLimiterPortOut {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean allow(String key, int maxAttempts, Duration window) {
        log.info("allow key: {}, maxAttempts: {}, window: {}", key, maxAttempts, window);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, window);
        }
        log.info("allow key: {}, maxAttempts: {}, window: {}", key, maxAttempts, window);
        return count != null && count <= maxAttempts;
    }

    @Override
    public long ttlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        // Return -2 if the key does not exist, -1 if the key exists but has no associated expire
        return ttl == null ? -1 : ttl;
    }

    @Override
    public void reset(String key) {
        log.info("reset key: {}", key);
        redisTemplate.delete(key);
    }
}
