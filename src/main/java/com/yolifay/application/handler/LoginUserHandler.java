package com.yolifay.application.handler;

import com.yolifay.application.command.LoginUserCommand;
import com.yolifay.application.dto.TokenPairResponse;
import com.yolifay.common.CommonConstants;
import com.yolifay.domain.service.PasswordHasherPort;
import com.yolifay.domain.port.read.UserQueryRepositoryPort;
import com.yolifay.domain.service.TokenIssuer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginUserHandler {
    private final UserQueryRepositoryPort userQueryRepo; // read-side credentials
    private final PasswordHasherPort hasher;
    private final TokenIssuer tokenIssuer;

    @Value("${jwt.access-expiration-minutes:60}")
    private long accessExpMinutes;

    // injeksi dari properties (Duration -> bisa "14d", "48h", dll.)
    @Value("${jwt.refresh-expiration:14d}")
    private Duration refreshExp;

    public TokenPairResponse executeLogin(LoginUserCommand cmd) {
        var creds = userQueryRepo.findByUsernameOrEmail(cmd.usernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException(CommonConstants.RESPONSE.ACCOUNT_NOT_FOUND.getDescription()));
        if (!hasher.matches(cmd.password(), creds.passwordHash())) {
            throw new IllegalArgumentException(CommonConstants.RESPONSE.INVALID_CREDENTIALS.getDescription());
        }

        // 2) issie Access token (JWT) + whitelist di Redis
        var access = tokenIssuer.issue(c.getId(), u.getUsername(), u.getEmail(), u.getFullName(), u.getRole());
        var accessTtl = Duration.between(access.issuedAt(), access.expiresAt());
        accessWhitelist.whitelist(access.jti(), u.getId(), accessTtl);

        // 3) Refresh token (opaque string di Redis; rotate di endpoint /refresh)
        String refresh = refreshStore.issue(u.getId(), refreshExp);
        Instant refreshExpAt = Instant.now().plus(refreshExp);

        // 4) Response
        return new TokenPairResponse(
                access.value(), access.issuedAt(), access.expiresAt(),
                refresh, refreshExpAt
        );

        var claims = Map.<String,Object>of(
                "uid", creds.id(),
                "username", creds.username(),
                "email", creds.email(),
                "roles", creds.roles()
        );
        return tokenProvider.issueAccessToken(String.valueOf(creds.id()), claims, accessExpMinutes);
    }

}
