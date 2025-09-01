package com.yolifay.domain.model.view;

import java.time.Instant;
import java.util.Set;

public record UserCredentialsView(
        Long id,
        String fullName,
        String username,
        String email,
        String phone,
        String passwordHash,
        Instant createdAt,
        Set<String> roles
) {
}
