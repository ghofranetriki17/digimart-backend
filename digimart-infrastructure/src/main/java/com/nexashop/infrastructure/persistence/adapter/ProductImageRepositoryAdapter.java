package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductImageRepository;
import com.nexashop.domain.catalog.entity.ProductImage;
import com.nexashop.infrastructure.persistence.jpa.ProductImageJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductImageMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductImageJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductImageRepositoryAdapter
        extends JpaRepositoryAdapter<ProductImage, ProductImageJpaEntity, Long>
        implements ProductImageRepository {

    private final ProductImageJpaRepository repository;

    public ProductImageRepositoryAdapter(ProductImageJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductImageJpaEntity toJpa(ProductImage domain) {
        return ProductImageMapper.toJpa(domain);
    }

    @Override
    protected ProductImage toDomain(ProductImageJpaEntity entity) {
        return ProductImageMapper.toDomain(entity);
    }

    @Override
    public List<ProductImage> findByProductId(Long productId) {
        return toDomainList(repository.findByProductIdOrderByDisplayOrderAsc(productId));
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }
}
