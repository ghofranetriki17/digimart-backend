package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductStoreInventoryRepository;
import com.nexashop.domain.catalog.entity.ProductStoreInventory;
import com.nexashop.infrastructure.persistence.jpa.ProductStoreInventoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductStoreInventoryMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductStoreInventoryJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductStoreInventoryRepositoryAdapter
        extends JpaRepositoryAdapter<ProductStoreInventory, ProductStoreInventoryJpaEntity, Long>
        implements ProductStoreInventoryRepository {

    private final ProductStoreInventoryJpaRepository repository;

    public ProductStoreInventoryRepositoryAdapter(ProductStoreInventoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductStoreInventoryJpaEntity toJpa(ProductStoreInventory domain) {
        return ProductStoreInventoryMapper.toJpa(domain);
    }

    @Override
    protected ProductStoreInventory toDomain(ProductStoreInventoryJpaEntity entity) {
        return ProductStoreInventoryMapper.toDomain(entity);
    }

    @Override
    public List<ProductStoreInventory> findByProductId(Long productId) {
        return toDomainList(repository.findByProductId(productId));
    }

    @Override
    public List<ProductStoreInventory> findByStoreId(Long storeId) {
        return toDomainList(repository.findByStoreId(storeId));
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }
}
