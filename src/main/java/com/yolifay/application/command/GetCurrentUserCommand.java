package com.yolifay.application.command;

import java.time.Instant;
import java.util.Set;

public record GetCurrentUserCommand(
        Long id,
        String fullName,
        String username,
        String email,
        String phoneNumber,
        String status,
        Instant createdAt,
        Instant updatedAt,
        long version,
        Set<String> roles
) {
}
