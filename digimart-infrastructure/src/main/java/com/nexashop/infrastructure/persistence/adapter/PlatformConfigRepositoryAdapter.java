package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.PlatformConfigRepository;
import com.nexashop.domain.billing.entity.PlatformConfig;
import com.nexashop.infrastructure.persistence.jpa.PlatformConfigJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.PlatformConfigJpaEntity;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PlatformConfigRepositoryAdapter
        extends JpaRepositoryAdapter<PlatformConfig, PlatformConfigJpaEntity, Long>
        implements PlatformConfigRepository {

    private final PlatformConfigJpaRepository repository;

    public PlatformConfigRepositoryAdapter(PlatformConfigJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected PlatformConfigJpaEntity toJpa(PlatformConfig domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected PlatformConfig toDomain(PlatformConfigJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public Optional<PlatformConfig> findByConfigKey(String configKey) {
        return repository.findByConfigKey(configKey).map(BillingMapper::toDomain);
    }
}
