package com.yolifay.infrastructure.adapter.in.web.dto;

public record TokenResponse(
        String access_token,
        String token_type,
        long expires_in,
        String scope,
        String jti,
        String refresh_token
) {
}
