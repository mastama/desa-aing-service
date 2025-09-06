package com.yolifay.application.handler;

import com.yolifay.application.command.LogoutCommand;
import com.yolifay.domain.port.out.ClockPortOut;
import com.yolifay.domain.port.out.JwtProviderPortOut;
import com.yolifay.domain.port.out.TokenStorePortOut;
import com.yolifay.infrastructure.adapter.out.audit.AuditAction;
import com.yolifay.infrastructure.adapter.out.audit.Audited;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutHandler {

    private final JwtProviderPortOut jwt;
    private final TokenStorePortOut tokenStore;
    private final ClockPortOut clock;

    @Transactional
    @Audited(action = AuditAction.LOGOUT)
    public void handleLogout(LogoutCommand cmd) {
        String token = extractBearer(cmd.authorizationHeader());
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("Missing Authorization header");
        }

        Map<String, Object> claims;
        try {
            claims = jwt.validateAndGetClaims(token);
        } catch (Exception ex) {
            log.warn("[LOGOUT] invalid access token: {}", ex.getMessage());
            throw new BadCredentialsException("Invalid access token");
        }

        String jti = (String) claims.get("jti");
        String sub = String.valueOf(claims.get("sub"));
        Instant exp = (Instant) claims.getOrDefault("exp", clock.now().plusSeconds(600));

        if (jti == null || sub == null) {
            throw new BadCredentialsException("Invalid access token claims");
        }

        // 1) Blacklist access token sampai masa berlaku habis
        tokenStore.blacklistAccess(jti, exp);

        // 2) Revoke semua refresh milik user (logout-all)
        Long userId = Long.valueOf(sub);
        tokenStore.revokeAllRefreshForUser(userId);

        log.info("[LOGOUT] userId={}, accessJti={} blacklisted until {}", userId, jti, exp);
    }

    private String extractBearer(String header) {
        if (header == null) return null;
        String h = header.trim();
        if (h.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            return h.substring(7).trim();
        }
        return h; // fallback: kalau hanya token tanpa prefix
    }
}
