package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.audit.entity.AuditEvent;
import com.nexashop.domain.audit.entity.AuditAction;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditEventRepository extends CrudRepositoryPort<AuditEvent, Long> {

    PageResult<AuditEvent> findByFilters(
            PageRequest request,
            Long tenantId,
            String entityType,
            AuditAction action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo
    );

    List<AuditEvent> findByFiltersForExport(
            Long tenantId,
            String entityType,
            AuditAction action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo,
            int limit
    );

    long deleteByOccurredAtBefore(LocalDateTime occurredBefore);
}
