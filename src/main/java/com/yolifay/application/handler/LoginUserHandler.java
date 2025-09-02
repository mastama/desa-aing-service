package com.yolifay.application.handler;

import com.yolifay.application.command.LoginUserCommand;
import com.yolifay.application.exception.RefreshStoreFailedException;
import com.yolifay.domain.model.Role;
import com.yolifay.domain.port.out.*;
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
public class LoginUserHandler {
    private final UserRepositoryPortOut userRepo;
    private final PasswordHasherPortOut hasher;
    private final JwtProviderPortOut jwt;
    private final TokenStorePortOut tokenStore;
    private final ClockPortOut clock;

    @Transactional
    public TokenResponse handleLogin(LoginUserCommand cmd) {
        final String usernameOrEmail = cmd.usernameOrEmail().toLowerCase();
        log.info("[LOGIN] attempt usernameOrEmail={}", usernameOrEmail);

        var user = userRepo.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or email or password"));

        // Cek status user
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("[LOGIN] userId={} status={} blocked", user.getId(), user.getStatus());
            throw new BadCredentialsException("User is not active");
        }

        if (!hasher.matches(cmd.password(), user.getPasswordHash().value())) {
            throw new BadCredentialsException("Invalid username or email or password");
        }

        var roles = user.getRoles().stream().map(Role::name).toList();
        // 2) issie Access token (JWT) (Terbitkan token)
        var at = jwt.issueAccess(user.getId(), roles, user.getVersion());
        var rt = jwt.issueRefresh(user.getId(), user.getVersion());
        tokenStore.storeRefreshToken(user.getId(), rt.jti(), rt.expiresAt(), cmd.ip(), cmd.userAgent());

        long expiresIn = Math.max(0, Duration.between(clock.now(), at.expiresAt()).toSeconds());
        // scope: kalau kamu mau dari roles -> scope, sederhanakan saja:
        String scope = String.join(" ", roles).replace("ROLE_", ""); // atau hardcode "READ WRITE" sesuai kebutuhan

        // Simpan refresh token di Redis (wajib berhasil - jika gagal, batalkan login)
        try {
            var ip = cmd.ip() == null ? "" : cmd.ip();
            var userAgent = cmd.userAgent() == null ? "" : cmd.userAgent();
            tokenStore.storeRefreshToken(user.getId(), rt.jti(), rt.expiresAt(), ip, userAgent);
        } catch (RuntimeException e) {
            log.error("[LOGIN] failed to store refresh token for userId={}", user.getId(), e);
            throw new RefreshStoreFailedException("Failed to store refresh token");
        }

        log.info("[LOGIN] success userId={} roles={} atExp={} rtExp={}",
                user.getId(), roles, at.expiresAt(), rt.expiresAt());

        return new TokenResponse(
                at.token(),
                "bearer",
                expiresIn,
                scope,
                at.jti(),
                // kalau ingin kirim refresh token di response:
                rt.token()
        );
    }
}
