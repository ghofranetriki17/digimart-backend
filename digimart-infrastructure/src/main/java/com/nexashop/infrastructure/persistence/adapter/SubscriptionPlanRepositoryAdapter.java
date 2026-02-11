package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.SubscriptionPlanRepository;
import com.nexashop.domain.billing.entity.SubscriptionPlan;
import com.nexashop.infrastructure.persistence.jpa.SubscriptionPlanJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.SubscriptionPlanJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class SubscriptionPlanRepositoryAdapter
        extends JpaRepositoryAdapter<SubscriptionPlan, SubscriptionPlanJpaEntity, Long>
        implements SubscriptionPlanRepository {

    private final SubscriptionPlanJpaRepository repository;

    public SubscriptionPlanRepositoryAdapter(SubscriptionPlanJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected SubscriptionPlanJpaEntity toJpa(SubscriptionPlan domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected SubscriptionPlan toDomain(SubscriptionPlanJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public Optional<SubscriptionPlan> findByCode(String code) {
        return repository.findByCode(code).map(BillingMapper::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findByActiveTrueOrderByNameAsc() {
        return toDomainList(repository.findByActiveTrueOrderByNameAsc());
    }

    @Override
    public PageResult<SubscriptionPlan> findByActiveTrueOrderByNameAsc(PageRequest request) {
        Page<SubscriptionPlanJpaEntity> page = repository.findByActiveTrueOrderByNameAsc(
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public List<SubscriptionPlan> findByStandardTrue() {
        return toDomainList(repository.findByStandardTrue());
    }

    @Override
    public PageResult<SubscriptionPlan> findAll(PageRequest request) {
        Page<SubscriptionPlanJpaEntity> page = repository.findAll(
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }
}
