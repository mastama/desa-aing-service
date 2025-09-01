package com.yolifay.application.command;

import com.yolifay.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserCommand(
        @NotBlank @Size(min=2,max=100) String fullName,
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_.\\-]{3,50}$") String username,
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^(\\\\+?62|0)[23489]\\\\d{8,11}$") String phoneNumber,
        @NotBlank @Size(min=6,max=100) String password,
        Role role // e.g., "USER", "ADMIN"
) {
    public Role roleOrDefault() {
        return role == null ? Role.USER : role;
    }
}
