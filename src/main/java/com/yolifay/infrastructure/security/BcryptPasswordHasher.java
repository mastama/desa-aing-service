package com.yolifay.infrastructure.security;

import com.yolifay.domain.service.PasswordHasherPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptPasswordHasher implements PasswordHasherPort {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String hash(String raw) {
        return encoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }
}
