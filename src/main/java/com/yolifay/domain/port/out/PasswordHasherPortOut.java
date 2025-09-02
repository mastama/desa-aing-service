package com.yolifay.domain.port.out;

public interface PasswordHasherPortOut {
    String hash(String raw);
    boolean matches(String raw, String hash);
}
