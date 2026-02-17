package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.PlanFeatureRepository;
import com.nexashop.domain.billing.entity.PlanFeature;
import com.nexashop.infrastructure.persistence.jpa.PlanFeatureJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.PlanFeatureJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PlanFeatureRepositoryAdapter
        extends JpaRepositoryAdapter<PlanFeature, PlanFeatureJpaEntity, Long>
        implements PlanFeatureRepository {

    private final PlanFeatureJpaRepository repository;

    public PlanFeatureRepositoryAdapter(PlanFeatureJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected PlanFeatureJpaEntity toJpa(PlanFeature domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected PlanFeature toDomain(PlanFeatureJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public List<PlanFeature> findByPlanId(Long planId) {
        return toDomainList(repository.findByPlanId(planId));
    }

    @Override
    public boolean existsByFeatureId(Long featureId) {
        return repository.existsByFeatureId(featureId);
    }
}
