package com.yolifay.domain.service;

public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String hashed);
}
