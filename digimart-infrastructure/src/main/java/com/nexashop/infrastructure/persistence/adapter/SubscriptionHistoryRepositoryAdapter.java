package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.SubscriptionHistoryRepository;
import com.nexashop.domain.billing.entity.SubscriptionHistory;
import com.nexashop.infrastructure.persistence.jpa.SubscriptionHistoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionHistoryJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionHistoryRepositoryAdapter
        extends JpaRepositoryAdapter<SubscriptionHistory, SubscriptionHistoryJpaEntity, Long>
        implements SubscriptionHistoryRepository {

    private final SubscriptionHistoryJpaRepository repository;

    public SubscriptionHistoryRepositoryAdapter(SubscriptionHistoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected SubscriptionHistoryJpaEntity toJpa(SubscriptionHistory domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected SubscriptionHistory toDomain(SubscriptionHistoryJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public List<SubscriptionHistory> findBySubscriptionIdOrderByPerformedAtDesc(Long subscriptionId) {
        return toDomainList(repository.findBySubscriptionIdOrderByPerformedAtDesc(subscriptionId));
    }

    @Override
    public List<SubscriptionHistory> findByTenantIdOrderByPerformedAtDesc(Long tenantId) {
        return toDomainList(repository.findByTenantIdOrderByPerformedAtDesc(tenantId));
    }
}
