package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.port.out.VariantOptionValueRepository;
import com.nexashop.domain.catalog.entity.VariantOptionValue;
import com.nexashop.infrastructure.persistence.jpa.VariantOptionValueJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.VariantOptionValueMapper;
import com.nexashop.infrastructure.persistence.model.catalog.VariantOptionValueJpaEntity;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class VariantOptionValueRepositoryAdapter
        extends JpaRepositoryAdapter<VariantOptionValue, VariantOptionValueJpaEntity, Long>
        implements VariantOptionValueRepository {

    private final VariantOptionValueJpaRepository repository;

    public VariantOptionValueRepositoryAdapter(VariantOptionValueJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected VariantOptionValueJpaEntity toJpa(VariantOptionValue domain) {
        return VariantOptionValueMapper.toJpa(domain);
    }

    @Override
    protected VariantOptionValue toDomain(VariantOptionValueJpaEntity entity) {
        return VariantOptionValueMapper.toDomain(entity);
    }

    @Override
    public List<VariantOptionValue> findByVariantId(Long variantId) {
        return toDomainList(repository.findByVariantId(variantId));
    }

    @Override
    public List<VariantOptionValue> findByVariantIds(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return List.of();
        }
        return toDomainList(repository.findByVariantIdIn(variantIds));
    }

    @Override
    @Transactional
    public void deleteByVariantId(Long variantId) {
        repository.deleteByVariantId(variantId);
    }

    @Override
    @Transactional
    public void deleteByVariantIds(List<Long> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) {
            return;
        }
        repository.deleteByVariantIdIn(variantIds);
    }
}
