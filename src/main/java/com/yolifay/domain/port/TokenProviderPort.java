package com.yolifay.domain.port;

import java.util.Map;

public interface TokenProviderPort {
    String issueAccessToken(String subject, Map<String,Object> claims, long minutesToExpire);
    Map<String,Object> validateAndGetClaims(String token, boolean allowExpired);
}
