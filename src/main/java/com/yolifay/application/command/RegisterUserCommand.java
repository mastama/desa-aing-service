package com.yolifay.application.command;

import com.yolifay.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserCommand(
        @NotBlank String fullName,
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String phoneNumber,
        Role roles
) {
    public Role roleOrDefault() {
        return roles == null ? Role.USER : roles;
    }
}
