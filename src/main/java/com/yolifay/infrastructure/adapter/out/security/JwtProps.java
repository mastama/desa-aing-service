package com.yolifay.infrastructure.adapter.out.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProps {

    @NotBlank
    private String issuer;

    // Opsi A (path file/resource)
    private String privateKeyPath;
    private String publicKeyPath;

    // Opsi B (isi PEM langsung) - optional fallback
    private String privateKeyPem;
    private String publicKeyPem;

    @Positive
    private long accessTtlSeconds = 600;

    @Positive
    private long refreshTtlSeconds = 1_209_600;

}


