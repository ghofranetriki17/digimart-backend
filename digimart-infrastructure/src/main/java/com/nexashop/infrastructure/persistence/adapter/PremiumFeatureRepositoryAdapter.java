package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.infrastructure.persistence.jpa.PremiumFeatureJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.PremiumFeatureJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PremiumFeatureRepositoryAdapter
        extends JpaRepositoryAdapter<PremiumFeature, PremiumFeatureJpaEntity, Long>
        implements PremiumFeatureRepository {

    private final PremiumFeatureJpaRepository repository;

    public PremiumFeatureRepositoryAdapter(PremiumFeatureJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected PremiumFeatureJpaEntity toJpa(PremiumFeature domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected PremiumFeature toDomain(PremiumFeatureJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public Optional<PremiumFeature> findByCode(String code) {
        return repository.findByCode(code).map(BillingMapper::toDomain);
    }

    @Override
    public List<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc() {
        return toDomainList(repository.findByActiveTrueOrderByDisplayOrderAsc());
    }
}
