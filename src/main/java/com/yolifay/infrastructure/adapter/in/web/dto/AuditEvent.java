package com.yolifay.infrastructure.adapter.in.web.dto;

import java.time.Instant;

public record AuditEvent(
        Long userId,
        String event,              // LOGIN, REGISTER, UPDATE_USER, ...
        String resource,            // /api/auth/login
        String method,              // GET/POST/PUT/DELETE
        String userAgent,           // user agent
        String ip,                  // ip address
        String reason,              // error/success reason (singkat, no sensitive)
        int status,                 // HTTP status
        Instant createdAt           // timestamp
) {
}
