package com.nexashop.application.usecase;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.exception.BadRequestException;
import com.nexashop.application.exception.ForbiddenException;
import com.nexashop.application.port.out.AuditEventRepository;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.audit.entity.AuditAction;
import com.nexashop.domain.audit.entity.AuditEvent;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuditLogUseCase {

    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final CurrentUserProvider currentUserProvider;
    private final AuditEventRepository auditEventRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    public AuditLogUseCase(
            CurrentUserProvider currentUserProvider,
            AuditEventRepository auditEventRepository,
            UserRepository userRepository,
            TenantRepository tenantRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.auditEventRepository = auditEventRepository;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
    }

    public PageResult<AuditLogEntry> listAuditEvents(
            PageRequest request,
            Long tenantId,
            String entityType,
            String action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo
    ) {
        Long scopedTenantId = resolveScopedTenantId(tenantId);
        AuditAction resolvedAction = resolveAuditAction(action);
        PageResult<AuditEvent> page = auditEventRepository.findByFilters(
                request,
                scopedTenantId,
                normalizeEntityType(entityType),
                resolvedAction,
                occurredFrom,
                occurredTo
        );
        Map<Long, ActorSnapshot> actorByUserId = loadActorSnapshots(page.items());
        Map<Long, String> tenantNameById = loadTenantNames(page.items());
        List<AuditLogEntry> items = page.items().stream()
                .map(event -> toAuditLogEntry(
                        event,
                        actorByUserId.get(event.getActorUserId()),
                        tenantNameById.get(event.getTenantId())
                ))
                .toList();
        return PageResult.of(items, page.page(), page.size(), page.totalItems());
    }

    public List<AuditLogEntry> listAuditEventsForExport(
            Long tenantId,
            String entityType,
            String action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo,
            int limit
    ) {
        Long scopedTenantId = resolveScopedTenantId(tenantId);
        AuditAction resolvedAction = resolveAuditAction(action);
        List<AuditEvent> events = auditEventRepository.findByFiltersForExport(
                scopedTenantId,
                normalizeEntityType(entityType),
                resolvedAction,
                occurredFrom,
                occurredTo,
                limit
        );
        Map<Long, ActorSnapshot> actorByUserId = loadActorSnapshots(events);
        Map<Long, String> tenantNameById = loadTenantNames(events);
        return events.stream()
                .map(event -> toAuditLogEntry(
                        event,
                        actorByUserId.get(event.getActorUserId()),
                        tenantNameById.get(event.getTenantId())
                ))
                .toList();
    }

    private Long resolveScopedTenantId(Long requestedTenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        boolean isSuperAdmin = currentUser.hasRole(SUPER_ADMIN_ROLE_CODE);
        if (isSuperAdmin) {
            return requestedTenantId;
        }
        Long currentTenantId = currentUser.tenantId();
        if (requestedTenantId != null && !requestedTenantId.equals(currentTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return currentTenantId;
    }

    private AuditAction resolveAuditAction(String rawAction) {
        if (rawAction == null || rawAction.isBlank()) {
            return null;
        }
        try {
            return AuditAction.valueOf(rawAction.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Unsupported action filter");
        }
    }

    private String normalizeEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) {
            return null;
        }
        return entityType.trim().toUpperCase(Locale.ROOT);
    }

    private Map<Long, ActorSnapshot> loadActorSnapshots(List<AuditEvent> events) {
        Map<Long, ActorSnapshot> actorByUserId = new HashMap<>();
        for (AuditEvent event : events) {
            if (event == null || event.getActorUserId() == null) {
                continue;
            }
            actorByUserId.computeIfAbsent(event.getActorUserId(), this::resolveActorSnapshot);
        }
        return actorByUserId;
    }

    private ActorSnapshot resolveActorSnapshot(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return new ActorSnapshot(userId, null, null);
        }
        String name = ((user.getFirstName() == null ? "" : user.getFirstName().trim()) + " "
                + (user.getLastName() == null ? "" : user.getLastName().trim())).trim();
        return new ActorSnapshot(userId, user.getEmail(), name.isBlank() ? null : name);
    }

    private Map<Long, String> loadTenantNames(List<AuditEvent> events) {
        Map<Long, String> tenantNames = new HashMap<>();
        for (AuditEvent event : events) {
            if (event == null || event.getTenantId() == null) {
                continue;
            }
            tenantNames.computeIfAbsent(event.getTenantId(), this::resolveTenantName);
        }
        return tenantNames;
    }

    private String resolveTenantName(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            return null;
        }
        String name = tenant.getName() == null ? "" : tenant.getName().trim();
        return name.isBlank() ? null : name;
    }

    private AuditLogEntry toAuditLogEntry(AuditEvent event, ActorSnapshot actor, String tenantName) {
        String actorEmail = actor == null ? null : actor.email();
        String actorName = actor == null ? null : actor.name();
        return new AuditLogEntry(
                event.getId(),
                event.getTenantId(),
                tenantName,
                event.getEntityType(),
                event.getEntityId(),
                event.getAction() == null ? null : event.getAction().name(),
                event.getBeforeJson(),
                event.getAfterJson(),
                event.getActorUserId(),
                actorEmail,
                actorName,
                event.getCorrelationId(),
                event.getOccurredAt(),
                event.getCreatedAt()
        );
    }

    private record ActorSnapshot(Long userId, String email, String name) {
    }

    public record AuditLogEntry(
            Long id,
            Long tenantId,
            String tenantName,
            String entityType,
            Long entityId,
            String action,
            String beforeJson,
            String afterJson,
            Long actorUserId,
            String actorEmail,
            String actorName,
            String correlationId,
            LocalDateTime occurredAt,
            LocalDateTime createdAt
    ) {
    }
}
