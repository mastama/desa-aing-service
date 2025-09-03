package com.yolifay.application.handler;

import com.yolifay.application.command.RefreshTokenCommand;
import com.yolifay.application.exception.RefreshStoreFailedException;
import com.yolifay.domain.port.out.ClockPortOut;
import com.yolifay.domain.port.out.JwtProviderPortOut;
import com.yolifay.domain.port.out.TokenStorePortOut;
import com.yolifay.domain.port.out.UserRepositoryPortOut;
import com.yolifay.infrastructure.adapter.in.web.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenHandler {

    private final JwtProviderPortOut jwtProvider;
    private final TokenStorePortOut tokenStore;
    private final UserRepositoryPortOut userRepo;
    private final ClockPortOut clock;

    @Transactional(readOnly = true)
    public TokenResponse handleRefreshToken(RefreshTokenCommand cmd) {
        log.info("Refresh token: {}", cmd);

        // 1. Parse and verify refresh token JWT
        var claims = jwtProvider.validateAndGetClaims(cmd.refreshToken());
        var userId = Long.parseLong((String) claims.get("sub"));
        var oldJti = (String) claims.get("jti");
        var verObj = claims.get("ver");
        var tokenVersion = (verObj instanceof Number n) ? n.longValue() : Long.parseLong(String.valueOf(verObj));
        var jwtExp = (Instant) claims.get("exp");

        // 2. Ambil user + cek version token
        var user = userRepo.findById(userId).orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (user.getVersion() != tokenVersion) {
            log.warn("[REFRESH] version mismatch userId={} tokenVer={} currentVer={}", userId, tokenVersion, user.getVersion());
            throw new RefreshStoreFailedException("Invalid refresh token");
        }

        // 3. Rotation and reuse detection
        if (tokenStore.refreshExists(userId, oldJti)) {
            // 3a. ROTATE: tandai lama sebagai 'used', hapus, terbitkan baru
            var originalExp = tokenStore.getRefreshExpiry(userId, oldJti).orElse(jwtExp);
            tokenStore.revokeRefresh(userId, oldJti);
            tokenStore.markRefreshAsUsed(userId, oldJti, originalExp);
            log.info("[REFRESH] rotate success userId={} oldJti={}", userId, oldJti);

            var roles = user.getRoles().stream().map(Enum::name).toList();

            var at = jwtProvider.issueAccess(user.getId(), roles, user.getVersion());
            var rt = jwtProvider.issueRefresh(user.getId(), user.getVersion());
            tokenStore.storeRefreshToken(user.getId(), rt.jti(), rt.expiresAt(), cmd.ipAddress(), cmd.userAgent());

            long expiresIn = Math.max(0, Duration.between(clock.now(), at.expiresAt()).toSeconds());
            var scope = String.join(" ", roles);
            return new TokenResponse(at.token(), "bearer", expiresIn, scope, at.jti(), rt.token());
        } else {
            // 3b. REUSE? Jika marker ada -> logout all, else invalid
            if (tokenStore.wasRefreshUsed(userId, oldJti)) {
                log.error("[REFRESH] reuse detected userId={} jti={}", userId, oldJti);
                tokenStore.revokeAllRefreshForUser(userId); // kill all sessions
            } else {
                log.warn("[REFRESH] unknown refresh jti userId={} jti={}", userId, oldJti);
            }
            throw new RefreshStoreFailedException("Invalid refresh token");
        }
    }
}
