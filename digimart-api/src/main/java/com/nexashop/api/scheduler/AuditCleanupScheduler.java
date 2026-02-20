package com.nexashop.api.scheduler;

import com.nexashop.application.usecase.AuditMaintenanceUseCase;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(prefix = "audit.cleanup", name = "enabled", havingValue = "true")
public class AuditCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuditCleanupScheduler.class);

    private final AuditMaintenanceUseCase auditMaintenanceUseCase;
    private final ZoneId cleanupZone;

    public AuditCleanupScheduler(
            AuditMaintenanceUseCase auditMaintenanceUseCase,
            @Value("${audit.cleanup.zone:Africa/Tunis}") String cleanupZoneId
    ) {
        this.auditMaintenanceUseCase = auditMaintenanceUseCase;
        this.cleanupZone = ZoneId.of(cleanupZoneId);
    }

    @Scheduled(cron = "${audit.cleanup.cron:0 1 0 1 * *}", zone = "${audit.cleanup.zone:Africa/Tunis}")
    @Transactional
    public void purgeMonthlyTenantAuditLogs() {
        LocalDateTime cutoff = LocalDate.now(cleanupZone).withDayOfMonth(1).atStartOfDay();
        long deleted = auditMaintenanceUseCase.purgeBefore(cutoff);
        log.info("Monthly audit cleanup done (cutoff={}, deletedRows={})", cutoff, deleted);
    }
}
