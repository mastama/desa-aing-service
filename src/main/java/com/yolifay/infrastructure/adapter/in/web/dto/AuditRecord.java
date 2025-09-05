package com.yolifay.infrastructure.adapter.in.web.dto;

public record AuditRecord(
        Long userId,
        String event,
        boolean success,
        String reason,
        String ip,
        String userAgent
) {
}
