package com.yolifay.domain.port.out;

import com.yolifay.infrastructure.adapter.in.web.dto.AuditEvent;

import java.util.List;

public interface AuditLogPortOut {
    void writeAudit(AuditEvent auditEvent);
    List<AuditEvent> findAll(int page, int size);
}
