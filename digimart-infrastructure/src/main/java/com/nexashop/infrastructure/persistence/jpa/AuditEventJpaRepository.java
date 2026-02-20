package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.infrastructure.persistence.model.audit.AuditEventJpaEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditEventJpaRepository
        extends JpaRepository<AuditEventJpaEntity, Long>, JpaSpecificationExecutor<AuditEventJpaEntity> {

    long deleteByOccurredAtLessThan(LocalDateTime occurredAt);
}
