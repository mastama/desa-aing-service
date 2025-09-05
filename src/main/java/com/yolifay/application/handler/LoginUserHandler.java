package com.yolifay.application.handler;

import com.yolifay.application.command.LoginUserCommand;
import com.yolifay.application.exception.DataNotFoundException;
import com.yolifay.application.exception.RefreshStoreFailedException;
import com.yolifay.application.exception.TooManyRequestsException;
import com.yolifay.application.exception.UserNotActiveException;
import com.yolifay.domain.model.Role;
import com.yolifay.domain.port.out.*;
import com.yolifay.infrastructure.adapter.in.web.dto.TokenResponse;
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
            auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(null, "LOGIN_RATE_LIMIT", false,
                    "ttl=" + ttl + "s", ip, userAgent));
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
                auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(uid, "LOGIN_BLOCKED", false,
                        "status=" + user.getStatus(), ip, userAgent));
                log.warn("[LOGIN] blocked status identifier={} status={}", identifier, user.getStatus());
                throw new UserNotActiveException("User is not active");
            }

            // 3. Verify password
            if (!hasher.matches(cmd.password(), user.getPasswordHash().value())) {
                auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(uid, "LOGIN_FAILED", false,
                        "bad credentials", ip, userAgent));
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
                auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(uid, "LOGIN_FAIL", false,
                        "refresh store failed: " + ex.getMessage(), ip, userAgent));
                log.error("[LOGIN] failed store refresh identifier={} err={}", identifier, ex.toString());
                throw new RefreshStoreFailedException("Failed to store refresh token");
            }
            // 6. Sukses -> reset rate limiter + audit log
            rateLimiter.reset(key);
            auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(uid, "LOGIN_SUCCESS", true,
                    "roles=" + String.join(",", roles), ip, userAgent));

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
        } catch (TooManyRequestsException ex) {
            // sudah di audit di atas
            throw ex;
        } catch (DataNotFoundException e) {
            // Kalau user belum ketemu (uid == null), tetap audit sebagai gagal kredensial
            if (uid == null) {
                auditLogPort.writeAudit(new AuditLogPortOut.AuditRecord(null, "LOGIN_FAIL", false,
                        "bad credentials", ip, userAgent));
            }
            throw e;
        }
    }
}
