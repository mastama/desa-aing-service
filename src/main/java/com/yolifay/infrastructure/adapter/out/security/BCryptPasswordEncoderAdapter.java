package com.yolifay.infrastructure.adapter.out.security;

import com.yolifay.domain.port.out.PasswordHasherPortOut;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordHasherPortOut {
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
    @Override public String hash(String raw){ return enc.encode(raw); }
    @Override public boolean matches(String raw, String hash){ return enc.matches(raw, hash); }
}
