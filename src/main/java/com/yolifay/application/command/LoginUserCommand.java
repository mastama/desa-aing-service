package com.yolifay.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserCommand(
        @NotBlank @Size(min=3,max=50) String usernameOrEmail,
        @NotBlank @Size(min=6,max=100) String password
) {
}
