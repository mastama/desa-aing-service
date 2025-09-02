package com.yolifay.domain.port.out;

import java.time.Instant;
import java.util.List;

public interface JwtProviderPortOut {
    record IssuedToken(String token, String jti, Instant expiresAt) {}
    IssuedToken issueAccess(Long userId, List<String> roles, long version);
    IssuedToken issueRefresh(Long userId, long version);
    java.util.Map<String,Object> validateAndGetClaims(String token);
}
