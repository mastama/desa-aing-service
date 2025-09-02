package com.yolifay.infrastructure.adapter.in.web.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
