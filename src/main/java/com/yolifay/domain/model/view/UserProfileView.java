package com.yolifay.domain.model.view;

import java.time.Instant;
import java.util.Set;

public record UserProfileView(
    Long id,
    String fullName,
    String username,
    String email,
    String phone,
    Instant createdAt,
    Set<String> roles
) {
}
