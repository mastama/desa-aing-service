package com.yolifay.infrastructure.adapter.out.persistence;

import com.yolifay.domain.port.out.AuditLogPortOut;
import com.yolifay.infrastructure.adapter.out.entity.AuthAuditEntity;
import com.yolifay.infrastructure.adapter.out.persistence.repository.AuthAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogJpaAdapter implements AuditLogPortOut {

    private final AuthAuditJpaRepository authAuditJpaRepo;

    @Override
    public void writeAudit(AuditRecord auditRec) {
        log.info("Audit record: {}", auditRec);

        var e = new AuthAuditEntity();
        e.setUserId(auditRec.userId());
        e.setEvent(auditRec.event());
        e.setSuccess(auditRec.success());
        e.setReason(auditRec.reason());
        e.setIp(auditRec.ip());
        e.setUserAgent(auditRec.userAgent());

        authAuditJpaRepo.save(e);
        log.info("Audit record saved: {}", e);
    }
}
