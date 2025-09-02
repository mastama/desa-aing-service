package com.yolifay.application.command;

public record LogoutCommand(
        String authorizationHeader
) {
}
