package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.TenantSubscriptionRepository;
import com.nexashop.domain.billing.entity.TenantSubscription;
import com.nexashop.domain.billing.enums.SubscriptionStatus;
import com.nexashop.infrastructure.persistence.jpa.TenantSubscriptionJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.TenantSubscriptionJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TenantSubscriptionRepositoryAdapter
        extends JpaRepositoryAdapter<TenantSubscription, TenantSubscriptionJpaEntity, Long>
        implements TenantSubscriptionRepository {

    private final TenantSubscriptionJpaRepository repository;

    public TenantSubscriptionRepositoryAdapter(TenantSubscriptionJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected TenantSubscriptionJpaEntity toJpa(TenantSubscription domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected TenantSubscription toDomain(TenantSubscriptionJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public List<TenantSubscription> findByTenantId(Long tenantId) {
        return toDomainList(repository.findByTenantId(tenantId));
    }

    @Override
    public boolean existsByPlanId(Long planId) {
        return repository.existsByPlanId(planId);
    }

    @Override
    public long countByPlanId(Long planId) {
        return repository.countByPlanId(planId);
    }

    @Override
    public Optional<TenantSubscription> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status) {
        return repository.findByTenantIdAndStatus(tenantId, status).map(BillingMapper::toDomain);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends TenantSubscription> S saveAndFlush(S entity) {
        TenantSubscriptionJpaEntity saved = repository.saveAndFlush(BillingMapper.toJpa(entity));
        return (S) BillingMapper.toDomain(saved);
    }
}
