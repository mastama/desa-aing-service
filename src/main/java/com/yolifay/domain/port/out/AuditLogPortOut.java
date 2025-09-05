package com.yolifay.domain.port.out;

import com.yolifay.infrastructure.adapter.in.web.dto.AuditRecord;

public interface AuditLogPortOut {
    void writeAudit(AuditRecord auditRec);
    record AuditRecord(Long userId, String event, boolean success, String reason, String ip, String userAgent) {
    }
}
