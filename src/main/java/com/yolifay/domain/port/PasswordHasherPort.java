package com.yolifay.domain.port;

public interface PasswordHasherPort {
    String hash(String raw);
    boolean matches(String raw, String hash);
}
