package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.VariantStoreInventoryRepository;
import com.nexashop.domain.catalog.entity.VariantStoreInventory;
import com.nexashop.infrastructure.persistence.jpa.VariantStoreInventoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.VariantStoreInventoryMapper;
import com.nexashop.infrastructure.persistence.model.catalog.VariantStoreInventoryJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class VariantStoreInventoryRepositoryAdapter
        extends JpaRepositoryAdapter<VariantStoreInventory, VariantStoreInventoryJpaEntity, Long>
        implements VariantStoreInventoryRepository {

    private final VariantStoreInventoryJpaRepository repository;

    public VariantStoreInventoryRepositoryAdapter(VariantStoreInventoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected VariantStoreInventoryJpaEntity toJpa(VariantStoreInventory domain) {
        return VariantStoreInventoryMapper.toJpa(domain);
    }

    @Override
    protected VariantStoreInventory toDomain(VariantStoreInventoryJpaEntity entity) {
        return VariantStoreInventoryMapper.toDomain(entity);
    }

    @Override
    public List<VariantStoreInventory> findByVariantId(Long variantId) {
        return toDomainList(repository.findByVariantId(variantId));
    }

    @Override
    public List<VariantStoreInventory> findByVariantIds(List<Long> variantIds) {
        return toDomainList(repository.findByVariantIdIn(variantIds));
    }

    @Override
    public List<VariantStoreInventory> findByStoreId(Long storeId) {
        return toDomainList(repository.findByStoreId(storeId));
    }

    @Override
    @Transactional
    public void deleteByVariantId(Long variantId) {
        repository.deleteByVariantId(variantId);
    }

    @Override
    @Transactional
    public void deleteByVariantIds(List<Long> variantIds) {
        repository.deleteByVariantIdIn(variantIds);
    }
}
