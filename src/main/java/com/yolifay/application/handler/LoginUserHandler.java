package com.yolifay.application.handler;

import com.yolifay.application.command.LoginUserCommand;
import com.yolifay.application.exception.DataNotFoundException;
import com.yolifay.application.exception.RefreshStoreFailedException;
import com.yolifay.application.exception.TooManyRequestsException;
import com.yolifay.application.exception.UserNotActiveException;
import com.yolifay.domain.model.Role;
import com.yolifay.domain.port.out.*;
import com.yolifay.infrastructure.adapter.in.web.dto.AuditEvent;
import com.yolifay.infrastructure.adapter.in.web.dto.TokenResponse;
import com.yolifay.infrastructure.adapter.out.audit.AuditAction;
import com.yolifay.infrastructure.adapter.out.audit.Audited;
import com.yolifay.infrastructure.config.RateLimitProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUserHandler {
    private final UserRepositoryPortOut userRepo;
    private final PasswordHasherPortOut hasher;
    private final JwtProviderPortOut jwt;
    private final TokenStorePortOut tokenStore;
    private final ClockPortOut clock;

    // rate limit
    private final RateLimiterPortOut rateLimiter;
    private final RateLimitProps rateLimitProps;

    // Audit log
    private final AuditLogPortOut auditLogPort;

    private String bucket(String uernameOrEmail) {
        return "auth:rate:login:%s".formatted(uernameOrEmail.toLowerCase());
    }

    @Transactional
    @Audited(action = AuditAction.LOGIN)
    public TokenResponse handleLogin(LoginUserCommand cmd) throws Exception {
        final String identifier = cmd.usernameOrEmail().toLowerCase();
        final String ip = cmd.ip() == null ? "" : cmd.ip();
        final String userAgent = cmd.userAgent() == null ? "" : cmd.userAgent();
        log.info("[LOGIN] attempt identifier={}", identifier);

        // rate limit
        String key = bucket(identifier);
        boolean allowed = rateLimiter.allow(key, rateLimitProps.maxAttempts(),
                Duration.ofSeconds(rateLimitProps.windowSeconds()));
        if (!allowed) {
            long ttl = Math.max(0, rateLimiter.ttlSeconds(key));
            log.warn("[LOGIN] rate limit exceeded identifier={} ttl={}s", identifier, ttl);
            throw new TooManyRequestsException("Too many login attempts. Please Try again in %ds".formatted(ttl));
        }

        Long uid = null;
        try {
            // 1. Find user by username or email (jangan bocorkan mana yang salah)
            var user = userRepo.findByUsernameOrEmail(identifier)
                    .orElseThrow(() -> new DataNotFoundException("Invalid username or email or password"));
            uid = user.getId();

            // 2. Check status
            if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                log.warn("[LOGIN] blocked status identifier={} status={}", identifier, user.getStatus());
                throw new UserNotActiveException("User is not active");
            }

            // 3. Verify password
            if (!hasher.matches(cmd.password(), user.getPasswordHash().value())) {
                log.warn("[LOGIN] invalid credentials identifier={}", identifier);
                throw new DataNotFoundException("Invalid username or email or password");
            }
            // 4. Issue JWT token
            var roles = user.getRoles().stream().map(Role::name).toList();
            var at = jwt.issueAccess(user.getId(), roles, user.getVersion()); // at == access token
            var rt = jwt.issueRefresh(user.getId(), user.getVersion()); // rt == refresh token

            // 5. Simpan ke token store (whitelist)
            try {
                tokenStore.storeRefreshToken(user.getId(), rt.jti(), rt.expiresAt(), ip, userAgent);
            } catch (RuntimeException ex) {
                log.error("[LOGIN] failed store refresh identifier={} err={}", identifier, ex.toString());
                throw new RefreshStoreFailedException("Failed to store refresh token");
            }
            // 6. Sukses -> reset rate limiter + audit log
            rateLimiter.reset(key);

            long expiresIn = Math.max(0, Duration.between(clock.now(), at.expiresAt()).toSeconds());
            String scope = String.join(" ", roles);

            log.info("[LOGIN] success userId={} roles={} atExp={} rtExp={}",
                    user.getId(), roles, at.expiresAt(), rt.expiresAt());

            return new TokenResponse(
                    at.token(),
                    "Bearer",
                    expiresIn,
                    scope,
                    at.jti(),
                    rt.token()
            );
        } catch (DataNotFoundException e) {
            // Kalau user belum ketemu (uid == null), tetap audit sebagai gagal kredensial
            if (uid == null) {
                auditLogPort.writeAudit(new AuditEvent(
                        null,
                        "AUTH_LOGIN",
                        "/api/v1/auth/login",
                        "POST",
                        userAgent,
                        ip,
                        "Invalid username or email or password",
                        401,
                        clock.now()
                ));
            } else {
                auditLogPort.writeAudit(new AuditEvent(
                        uid,
                        "AUTH_LOGIN",
                        "/api/v1/auth/login",
                        "POST",
                        userAgent,
                        ip,
                        "Invalid username or email or password",
                        401,
                        clock.now()
                ));
            }
            throw e;
        }
    }
}
