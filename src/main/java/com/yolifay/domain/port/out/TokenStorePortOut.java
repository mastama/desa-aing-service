package com.yolifay.domain.port.out;

import java.time.Instant;
import java.util.Optional;

public interface TokenStorePortOut {

    // Refresh tokens
    void storeRefreshToken(Long userId, String refreshJti, Instant expiresAt, String ip, String userAgent);
    boolean refreshExists(Long userId, String refreshJti);
    void revokeRefresh(Long userId, String refreshJti);
    void revokeAllRefreshForUser(Long userId); // opsional (logout all)
    Optional<Instant> getRefreshExpiry(Long userId, String refreshJti);

    // Rotation or reuse detection
    void markRefreshAsUsed(Long userId, String refreshJti, Instant originalExpiresAt);
    boolean wasRefreshUsed(Long userId, String refreshJti);

    // Access blacklist
    void blacklistAccess(String accessJti, Instant expiresAt);
    boolean isAccessBlacklisted(String accessJti);
}
