package com.yolifay.infrastructure.config;

import com.yolifay.domain.port.out.ClockPortOut;
import com.yolifay.domain.port.out.IdGeneratorPortOut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.UUID;

@Configuration
public class BeansConfig {

    @Bean
    public ClockPortOut clockPort() {
        return Instant::now; // implementasi simple
    }

    @Bean
    public IdGeneratorPortOut idGeneratorPort() {
        return () -> UUID.randomUUID().toString();
    }
}
