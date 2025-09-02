package com.yolifay.application.command;

public record LoginUserCommand(
        String usernameOrEmail,
        String password,
        String ip,
        String userAgent
) {
}
