package com.yolifay.infrastructure.adapter.out.persistence;

import com.yolifay.domain.port.out.AuditLogPortOut;
import com.yolifay.infrastructure.adapter.in.web.dto.AuditEvent;
import com.yolifay.infrastructure.adapter.out.entity.AuthAuditEntity;
import com.yolifay.infrastructure.adapter.out.persistence.repository.AuthAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogJpaAdapter implements AuditLogPortOut {

    private final AuthAuditJpaRepository repo;

    @Override
    public void writeAudit(AuditEvent auditEvent) {
        var e = new AuthAuditEntity();
        e.setUserId(auditEvent.userId());
        e.setEvent(auditEvent.event());
        e.setResource(auditEvent.resource());
        e.setMethod(auditEvent.method());
        e.setUserAgent(auditEvent.userAgent());
        e.setIp(auditEvent.ip());
        e.setReason(auditEvent.reason());
        e.setStatus(auditEvent.status());
        e.setCreatedAt(auditEvent.createdAt() != null ? auditEvent.createdAt() : Instant.now());
        repo.save(e);
    }

    @Override
    public List<AuditEvent> findAll(int page, int size) {
        return List.of();
    }
}
