package com.yolifay.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank String refreshToken
) {
}
