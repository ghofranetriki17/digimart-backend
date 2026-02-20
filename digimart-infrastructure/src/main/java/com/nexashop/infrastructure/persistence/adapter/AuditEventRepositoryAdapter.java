package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.AuditEventRepository;
import com.nexashop.domain.audit.entity.AuditAction;
import com.nexashop.domain.audit.entity.AuditEvent;
import com.nexashop.infrastructure.persistence.jpa.AuditEventJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.AuditMapper;
import com.nexashop.infrastructure.persistence.model.audit.AuditEventJpaEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
public class AuditEventRepositoryAdapter
        extends JpaRepositoryAdapter<AuditEvent, AuditEventJpaEntity, Long>
        implements AuditEventRepository {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"));

    private final AuditEventJpaRepository repository;

    public AuditEventRepositoryAdapter(AuditEventJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected AuditEventJpaEntity toJpa(AuditEvent domain) {
        return AuditMapper.toJpa(domain);
    }

    @Override
    protected AuditEvent toDomain(AuditEventJpaEntity entity) {
        return AuditMapper.toDomain(entity);
    }

    @Override
    public PageResult<AuditEvent> findByFilters(
            PageRequest request,
            Long tenantId,
            String entityType,
            AuditAction action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo
    ) {
        Specification<AuditEventJpaEntity> specification = buildSpecification(
                tenantId,
                entityType,
                action,
                occurredFrom,
                occurredTo
        );
        Page<AuditEventJpaEntity> page = repository.findAll(
                specification,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size(), DEFAULT_SORT)
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public List<AuditEvent> findByFiltersForExport(
            Long tenantId,
            String entityType,
            AuditAction action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 10_000));
        Specification<AuditEventJpaEntity> specification = buildSpecification(
                tenantId,
                entityType,
                action,
                occurredFrom,
                occurredTo
        );
        Page<AuditEventJpaEntity> page = repository.findAll(
                specification,
                org.springframework.data.domain.PageRequest.of(0, safeLimit, DEFAULT_SORT)
        );
        return toDomainList(page.getContent());
    }

    @Override
    public long deleteByOccurredAtBefore(LocalDateTime occurredBefore) {
        if (occurredBefore == null) {
            return 0L;
        }
        return repository.deleteByOccurredAtLessThan(occurredBefore);
    }

    private Specification<AuditEventJpaEntity> buildSpecification(
            Long tenantId,
            String entityType,
            AuditAction action,
            LocalDateTime occurredFrom,
            LocalDateTime occurredTo
    ) {
        Specification<AuditEventJpaEntity> specification = (root, query, cb) -> cb.conjunction();
        if (tenantId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("tenantId"), tenantId));
        }
        if (entityType != null && !entityType.isBlank()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (action != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (occurredFrom != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), occurredFrom));
        }
        if (occurredTo != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), occurredTo));
        }
        return specification;
    }
}
