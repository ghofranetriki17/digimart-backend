package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductOptionRepository;
import com.nexashop.domain.catalog.entity.ProductOption;
import com.nexashop.infrastructure.persistence.jpa.ProductOptionJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductOptionMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductOptionRepositoryAdapter
        extends JpaRepositoryAdapter<ProductOption, ProductOptionJpaEntity, Long>
        implements ProductOptionRepository {

    private final ProductOptionJpaRepository repository;

    public ProductOptionRepositoryAdapter(ProductOptionJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductOptionJpaEntity toJpa(ProductOption domain) {
        return ProductOptionMapper.toJpa(domain);
    }

    @Override
    protected ProductOption toDomain(ProductOptionJpaEntity entity) {
        return ProductOptionMapper.toDomain(entity);
    }

    @Override
    public List<ProductOption> findByProductId(Long productId) {
        return toDomainList(repository.findByProductId(productId));
    }

    @Override
    @Transactional
    public void deleteByProductId(Long productId) {
        repository.deleteByProductId(productId);
    }
}
