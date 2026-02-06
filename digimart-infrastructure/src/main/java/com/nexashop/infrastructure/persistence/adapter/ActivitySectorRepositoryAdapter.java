package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.infrastructure.persistence.jpa.ActivitySectorJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.TenantMapper;
import com.nexashop.infrastructure.persistence.model.tenant.ActivitySectorJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ActivitySectorRepositoryAdapter
        extends JpaRepositoryAdapter<ActivitySector, ActivitySectorJpaEntity, Long>
        implements ActivitySectorRepository {

    private final ActivitySectorJpaRepository repository;

    public ActivitySectorRepositoryAdapter(ActivitySectorJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ActivitySectorJpaEntity toJpa(ActivitySector domain) {
        return TenantMapper.toJpa(domain);
    }

    @Override
    protected ActivitySector toDomain(ActivitySectorJpaEntity entity) {
        return TenantMapper.toDomain(entity);
    }

    @Override
    public Optional<ActivitySector> findByLabelIgnoreCase(String label) {
        return repository.findByLabelIgnoreCase(label).map(TenantMapper::toDomain);
    }

    @Override
    public List<ActivitySector> findByActiveTrue() {
        return toDomainList(repository.findByActiveTrue());
    }
}
