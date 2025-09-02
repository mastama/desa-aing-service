package com.yolifay.infrastructure.adapter.in.web.dto;

import java.time.Instant;
import java.util.Set;

public record RegisterResponse(
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
