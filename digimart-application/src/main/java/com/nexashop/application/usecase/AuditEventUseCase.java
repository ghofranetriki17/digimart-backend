package com.nexashop.application.usecase;

import com.nexashop.application.port.out.AuditEventRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.audit.entity.AuditAction;
import com.nexashop.domain.audit.entity.AuditEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public class AuditEventUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AuditEventRepository auditEventRepository;

    public AuditEventUseCase(
            CurrentUserProvider currentUserProvider,
            AuditEventRepository auditEventRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.auditEventRepository = auditEventRepository;
    }

    public void recordSuccess(
            AuditAction action,
            String entityType,
            Long entityId,
            String beforeJson,
            String afterJson,
            String correlationId
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setTenantId(currentUser.tenantId());
        auditEvent.setEntityType(entityType);
        auditEvent.setEntityId(entityId);
        auditEvent.setAction(action);
        auditEvent.setBeforeJson(beforeJson);
        auditEvent.setAfterJson(afterJson);
        auditEvent.setActorUserId(currentUser.userId());
        auditEvent.setCorrelationId(resolveCorrelationId(correlationId));
        auditEvent.setOccurredAt(LocalDateTime.now());
        auditEventRepository.save(auditEvent);
    }

    private String resolveCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId.trim();
    }
}
