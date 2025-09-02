package com.yolifay.infrastructure.adapter.out.cache;

import com.yolifay.domain.port.out.TokenStorePortOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class RedisTokenStoreAdapter implements TokenStorePortOut {

    private final StringRedisTemplate redis;

    public RedisTokenStoreAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String kRefresh(Long uid, String jti) { return "auth:refresh:%d:%s".formatted(uid, jti); }
    private static String kAccessBL(String jti) { return "auth:blacklist:access:%s".formatted(jti); }

    @Override
    public void storeRefreshToken(Long userId, String refreshJti, Instant expiresAt, String ip, String ua) {
        log.info("Storing refresh token for userId= {}", userId);
        var key = kRefresh(userId, refreshJti);
        var ttl = Duration.between(Instant.now(), expiresAt);
        redis.opsForHash().put(key, "ip", ip == null ? "" : ip);
        redis.opsForHash().put(key, "ua", ua == null ? "" : ua);
        redis.opsForHash().put(key, "exp", String.valueOf(expiresAt.getEpochSecond()));
        redis.expire(key, ttl.isNegative() ? Duration.ofSeconds(1) : ttl);
    }

    @Override public boolean refreshExists(Long userId, String refreshJti) {
        log.info("Refresh exists for userId= {}", userId);
        return Boolean.TRUE.equals(redis.hasKey(kRefresh(userId, refreshJti)));
    }

    @Override public void revokeRefresh(Long userId, String refreshJti) {
        log.info("Revoking refresh token for userId= {}", userId);
        redis.delete(kRefresh(userId, refreshJti));
    }

    @Override
    public void revokeAllRefreshForUser(Long userId) {
        log.info("Revoking all refresh tokens for userId= {}", userId);
        var pattern = "auth:refresh:%d:*".formatted(userId);
        var conn = Objects.requireNonNull(redis.getConnectionFactory()).getConnection();
        var scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions().match(pattern).count(1000).build();
        var keys = new java.util.ArrayList<byte[]>();
        var cursor = conn.keyCommands().scan(scanOptions);
        cursor.forEachRemaining(keys::add);
        if (!keys.isEmpty()) {
            conn.keyCommands().del(keys.toArray(new byte[0][]));
        }
    }


    @Override public Optional<Instant> getRefreshExpiry(Long userId, String refreshJti) {
        log.info("Getting refresh token expiry for userId= {}", userId);
        var v = (String) redis.opsForHash().get(kRefresh(userId, refreshJti), "exp");
        return v == null ? Optional.empty() : Optional.of(Instant.ofEpochSecond(Long.parseLong(v)));
    }

    @Override
    public void blacklistAccess(String accessJti, Instant expiresAt) {
        log.info("Blacklisting access token jti= {}", accessJti);
        var ttl = Duration.between(Instant.now(), expiresAt);
        redis.opsForValue().set(kAccessBL(accessJti), "1", ttl.isNegative() ? Duration.ofSeconds(1) : ttl);
    }

    @Override
    public boolean isAccessBlacklisted(String accessJti) {
        log.info("Checking if access token jti= {} is blacklisted", accessJti);
        return Boolean.TRUE.equals(redis.hasKey(kAccessBL(accessJti)));
    }
}