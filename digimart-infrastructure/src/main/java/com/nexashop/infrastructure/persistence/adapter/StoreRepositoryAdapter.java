package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.StoreRepository;
import com.nexashop.domain.store.entity.Store;
import com.nexashop.infrastructure.persistence.jpa.StoreJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.StoreMapper;
import com.nexashop.infrastructure.persistence.model.store.StoreJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class StoreRepositoryAdapter
        extends JpaRepositoryAdapter<Store, StoreJpaEntity, Long>
        implements StoreRepository {

    private final StoreJpaRepository repository;

    public StoreRepositoryAdapter(StoreJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected StoreJpaEntity toJpa(Store domain) {
        return StoreMapper.toJpa(domain);
    }

    @Override
    protected Store toDomain(StoreJpaEntity entity) {
        return StoreMapper.toDomain(entity);
    }

    @Override
    public boolean existsByTenantIdAndCode(Long tenantId, String code) {
        return repository.existsByTenantIdAndCode(tenantId, code);
    }

    @Override
    public Optional<Store> findByIdAndTenantId(Long id, Long tenantId) {
        return repository.findByIdAndTenantId(id, tenantId).map(StoreMapper::toDomain);
    }

    @Override
    public List<Store> findByTenantId(Long tenantId) {
        return toDomainList(repository.findByTenantId(tenantId));
    }

    @Override
    public List<Store> findByTenantIdIn(List<Long> tenantIds) {
        return toDomainList(repository.findByTenantIdIn(tenantIds));
    }
}
