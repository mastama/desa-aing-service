package com.yolifay.domain.valueobject;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or empty");
        }
    }
}
