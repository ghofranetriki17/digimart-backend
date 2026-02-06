package com.nexashop.infrastructure.persistence.model.audit;

import com.nexashop.domain.audit.entity.AuditAction;
import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "audit_events")
@Getter
@Setter
public class AuditEventJpaEntity extends TenantScopedJpaEntity {

    @Column(nullable = false)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(columnDefinition = "jsonb")
    private String beforeJson;

    @Column(columnDefinition = "jsonb")
    private String afterJson;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    private String ipAddress;

    private String userAgent;

    @Column(nullable = false)
    private String correlationId;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @PrePersist
    protected void onPersist() {
        if (occurredAt == null) {
            occurredAt = LocalDateTime.now();
        }
    }
}
