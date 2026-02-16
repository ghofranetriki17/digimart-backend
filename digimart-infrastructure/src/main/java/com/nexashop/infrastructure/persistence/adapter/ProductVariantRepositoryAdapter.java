package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductVariantRepository;
import com.nexashop.domain.catalog.entity.ProductVariant;
import com.nexashop.infrastructure.persistence.jpa.ProductVariantJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductVariantMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductVariantJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductVariantRepositoryAdapter
        extends JpaRepositoryAdapter<ProductVariant, ProductVariantJpaEntity, Long>
        implements ProductVariantRepository {

    private final ProductVariantJpaRepository repository;

    public ProductVariantRepositoryAdapter(ProductVariantJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductVariantJpaEntity toJpa(ProductVariant domain) {
        return ProductVariantMapper.toJpa(domain);
    }

    @Override
    protected ProductVariant toDomain(ProductVariantJpaEntity entity) {
        return ProductVariantMapper.toDomain(entity);
    }

    @Override
    public List<ProductVariant> findByProductId(Long productId) {
        return toDomainList(repository.findByProductId(productId));
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }

    @Override
    public boolean existsByProductId(Long productId) {
        return repository.existsByProductId(productId);
    }

    @Override
    public boolean existsByTenantIdAndSku(Long tenantId, String sku) {
        return repository.existsByTenantIdAndSku(tenantId, sku);
    }

    @Override
    public boolean existsByTenantIdAndSkuAndProductIdNot(Long tenantId, String sku, Long productId) {
        return repository.existsByTenantIdAndSkuAndProductIdNot(tenantId, sku, productId);
    }

    @Override
    public boolean existsLowStockByProductId(Long productId) {
        return repository.existsLowStockByProductId(productId);
    }

    @Override
    public boolean existsDefaultVariant(Long productId) {
        return repository.existsDefaultVariant(productId);
    }

    @Override
    @Transactional
    public void clearProductImage(Long productId, Long productImageId) {
        repository.clearProductImage(productId, productImageId);
    }
}
