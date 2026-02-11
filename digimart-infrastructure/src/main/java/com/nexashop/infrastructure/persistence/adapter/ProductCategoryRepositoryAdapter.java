package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductCategoryRepository;
import com.nexashop.domain.catalog.entity.ProductCategory;
import com.nexashop.infrastructure.persistence.jpa.ProductCategoryJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductCategoryMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductCategoryJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductCategoryRepositoryAdapter
        extends JpaRepositoryAdapter<ProductCategory, ProductCategoryJpaEntity, Long>
        implements ProductCategoryRepository {

    private final ProductCategoryJpaRepository repository;

    public ProductCategoryRepositoryAdapter(ProductCategoryJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductCategoryJpaEntity toJpa(ProductCategory domain) {
        return ProductCategoryMapper.toJpa(domain);
    }

    @Override
    protected ProductCategory toDomain(ProductCategoryJpaEntity entity) {
        return ProductCategoryMapper.toDomain(entity);
    }

    @Override
    public List<ProductCategory> findByProductId(Long productId) {
        return toDomainList(repository.findByProductId(productId));
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }
}
