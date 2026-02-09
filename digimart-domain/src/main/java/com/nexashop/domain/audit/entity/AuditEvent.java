package com.nexashop.domain.audit.entity;

import com.nexashop.domain.common.TenantEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuditEvent extends TenantEntity {

    private String entityType;

    private Long entityId;

    private AuditAction action;

    private String beforeJson;

    private String afterJson;

    private Long actorUserId;

    private String ipAddress;

    private String userAgent;

    private String correlationId;

    private LocalDateTime occurredAt;

    protected void onPersist() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
