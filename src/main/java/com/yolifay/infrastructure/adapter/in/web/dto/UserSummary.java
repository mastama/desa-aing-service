package com.yolifay.infrastructure.adapter.in.web.dto;

import com.yolifay.domain.model.Role;

import java.util.Set;

public record UserSummary(
        Long id,
        String fullName,
        String username,
        String email,
        String status,
        Set<Role> roles
) {
}
