package com.nexashop.application.usecase;

import com.nexashop.application.port.out.AuditEventRepository;
import java.time.LocalDateTime;

public class AuditMaintenanceUseCase {

    private final AuditEventRepository auditEventRepository;

    public AuditMaintenanceUseCase(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public long purgeBefore(LocalDateTime cutoff) {
        if (cutoff == null) {
            return 0L;
        }
        return auditEventRepository.deleteByOccurredAtBefore(cutoff);
    }
}
