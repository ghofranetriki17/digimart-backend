package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.ProductOptionValueRepository;
import com.nexashop.domain.catalog.entity.ProductOptionValue;
import com.nexashop.infrastructure.persistence.jpa.ProductOptionValueJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.ProductOptionValueMapper;
import com.nexashop.infrastructure.persistence.model.catalog.ProductOptionValueJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductOptionValueRepositoryAdapter
        extends JpaRepositoryAdapter<ProductOptionValue, ProductOptionValueJpaEntity, Long>
        implements ProductOptionValueRepository {

    private final ProductOptionValueJpaRepository repository;

    public ProductOptionValueRepositoryAdapter(ProductOptionValueJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected ProductOptionValueJpaEntity toJpa(ProductOptionValue domain) {
        return ProductOptionValueMapper.toJpa(domain);
    }

    @Override
    protected ProductOptionValue toDomain(ProductOptionValueJpaEntity entity) {
        return ProductOptionValueMapper.toDomain(entity);
    }

    @Override
    public List<ProductOptionValue> findByOptionId(Long optionId) {
        return toDomainList(repository.findByOptionId(optionId));
    }

    @Override
    public List<ProductOptionValue> findByOptionIds(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }
        return toDomainList(repository.findByOptionIdIn(optionIds));
    }

    @Override
    @Transactional
    public void deleteByOptionId(Long optionId) {
        repository.deleteByOptionId(optionId);
    }

    @Override
    @Transactional
    public void deleteByOptionIds(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return;
        }
        repository.deleteByOptionIdIn(optionIds);
    }
}
