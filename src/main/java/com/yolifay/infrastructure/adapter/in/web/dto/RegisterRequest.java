package com.yolifay.infrastructure.adapter.in.web.dto;

import com.yolifay.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @NotBlank @Size(min=3, max=150) String fullName,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,100}$",
                message = "username may contain letters, numbers, underscore, dot, dash (3-100 chars)")
        String username,
        @NotBlank @Size(min=8, max=100) String password,
        @Pattern(regexp = "^\\+?\\d{8,15}$", message = "invalid phone number")
        String phoneNumber,
        Role roles
) {
}
