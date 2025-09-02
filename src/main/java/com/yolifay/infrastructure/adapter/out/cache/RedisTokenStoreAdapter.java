package com.yolifay.infrastructure.adapter.out.cache;

import com.yolifay.domain.port.out.TokenStorePortOut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
@Slf4j
public class RedisTokenStoreAdapter implements TokenStorePortOut {

    private final StringRedisTemplate redis;

    public RedisTokenStoreAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private static String kRefresh(Long uid, String jti) { return "auth:refresh:%d:%s".formatted(uid, jti); }
    private static String kRefreshIdx(Long uid) { return "auth:refresh:index:%d".formatted(uid); }
    private static String kRefreshUsed(Long uid, String jti) { return "auth:refresh:used:%d:%s".formatted(uid, jti); }
    private static String kAccessBL(String jti) { return "auth:blacklist:access:%s".formatted(jti); }

    @Override
    public void storeRefreshToken(Long userId, String refreshJti, Instant expiresAt, String ip, String ua) {
        log.info("Storing refresh token for userId= {}", userId);

        var key = kRefresh(userId, refreshJti);
        var ttl = Duration.between(Instant.now(), expiresAt);
        var ttlSafe = ttl.isNegative() ? Duration.ofSeconds(1) : ttl;

        redis.opsForHash().put(key, "sub", String.valueOf(userId));
        redis.opsForHash().put(key, "jti", refreshJti);
        redis.opsForHash().put(key, "exp", String.valueOf(expiresAt.getEpochSecond()));
        redis.opsForHash().put(key, "ip",  ip == null ? "" : ip);
        redis.opsForHash().put(key, "ua",  ua == null ? "" : ua);
        redis.expire(key, ttlSafe);

        // (opsional) index per user -> set jti
        redis.opsForSet().add(kRefreshIdx(userId), refreshJti);
        redis.expire(kRefreshIdx(userId), ttlSafe);
    }

    @Override public boolean refreshExists(Long userId, String refreshJti) {
        log.info("Refresh exists for userId= {}", userId);
        return Boolean.TRUE.equals(redis.hasKey(kRefresh(userId, refreshJti)));
    }

    @Override public void revokeRefresh(Long userId, String refreshJti) {
        log.info("Revoking refresh token for userId= {}", userId);
        redis.delete(kRefresh(userId, refreshJti));
        log.info("Refresh deleted.");
        redis.opsForSet().remove(kRefreshIdx(userId), refreshJti);
    }

    @Override
    public void revokeAllRefreshForUser(Long userId) {
        log.info("Revoking all refresh tokens for userId= {}", userId);

        var idxKey = kRefreshIdx(userId);
        var jtIs = redis.opsForSet().members(idxKey);
        if (jtIs != null && !jtIs.isEmpty()) {
            var keys = jtIs.stream()
                    .map(jti -> kRefresh(userId, jti)).toList();
            redis.delete(keys);
            log.info("Deleted {} refresh tokens.", keys.size());
            redis.delete(idxKey);
        }
    }

    @Override
    public Optional<Instant> getRefreshExpiry(Long userId, String refreshJti) {
        log.info("Getting refresh token expiry for userId= {}", userId);
        var v = (String) redis.opsForHash().get(kRefresh(userId, refreshJti), "exp");
        return v == null ? Optional.empty() : Optional.of(Instant.ofEpochSecond(Long.parseLong(v)));
    }

    // Rotation or reuse maker
    @Override
    public void markRefreshAsUsed(Long userId, String refreshJti, Instant originalExpiresAt) {
        log.info("Marking refresh usage for userId= {}", userId);
        var ttl = Duration.between(Instant.now(), originalExpiresAt);
        log.info("Marking refresh token as used for userId= {}, ttl= {}", userId, ttl);
        var safe = ttl.isNegative() ? Duration.ofSeconds(1) : ttl;
        redis.opsForValue().set(kRefreshUsed(userId, refreshJti), "1", safe);
    }

    @Override
    public boolean wasRefreshUsed(Long userId, String refreshJti) {
        log.info("Getting was refresh used for userId= {}", userId);
        return Boolean.TRUE.equals(redis.hasKey(kRefresh(userId, refreshJti)));
    }

    // Access blacklist
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