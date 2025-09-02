package com.yolifay.application.command;

public record RefreshTokenCommand(
        String refreshToken,
        String ipAddress,
        String userAgent
) {
}
