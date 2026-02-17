package com.nexashop.infrastructure.persistence.adapter;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.application.port.out.PremiumFeatureRepository;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.enums.FeatureCategory;
import com.nexashop.infrastructure.persistence.jpa.PremiumFeatureJpaRepository;
import com.nexashop.infrastructure.persistence.mapper.BillingMapper;
import com.nexashop.infrastructure.persistence.model.billing.PremiumFeatureJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

@Repository
public class PremiumFeatureRepositoryAdapter
        extends JpaRepositoryAdapter<PremiumFeature, PremiumFeatureJpaEntity, Long>
        implements PremiumFeatureRepository {

    private final PremiumFeatureJpaRepository repository;

    public PremiumFeatureRepositoryAdapter(PremiumFeatureJpaRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    protected PremiumFeatureJpaEntity toJpa(PremiumFeature domain) {
        return BillingMapper.toJpa(domain);
    }

    @Override
    protected PremiumFeature toDomain(PremiumFeatureJpaEntity entity) {
        return BillingMapper.toDomain(entity);
    }

    @Override
    public Optional<PremiumFeature> findByCode(String code) {
        return repository.findByCode(code).map(BillingMapper::toDomain);
    }

    @Override
    public List<PremiumFeature> findByCategoryOrderByDisplayOrderAsc(FeatureCategory category) {
        return toDomainList(repository.findByCategoryOrderByDisplayOrderAsc(category));
    }

    @Override
    public List<PremiumFeature> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(FeatureCategory category) {
        return toDomainList(repository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(category));
    }

    @Override
    public List<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc() {
        return toDomainList(repository.findByActiveTrueOrderByDisplayOrderAsc());
    }

    @Override
    public PageResult<PremiumFeature> findByCategoryOrderByDisplayOrderAsc(PageRequest request, FeatureCategory category) {
        Page<PremiumFeatureJpaEntity> page = repository.findByCategoryOrderByDisplayOrderAsc(
                category,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public PageResult<PremiumFeature> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(PageRequest request, FeatureCategory category) {
        Page<PremiumFeatureJpaEntity> page = repository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(
                category,
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public PageResult<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc(PageRequest request) {
        Page<PremiumFeatureJpaEntity> page = repository.findByActiveTrueOrderByDisplayOrderAsc(
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }

    @Override
    public PageResult<PremiumFeature> findAll(PageRequest request) {
        Page<PremiumFeatureJpaEntity> page = repository.findAll(
                org.springframework.data.domain.PageRequest.of(request.page(), request.size())
        );
        return PageResult.of(
                toDomainList(page.getContent()),
                request.page(),
                request.size(),
                page.getTotalElements()
        );
    }
}
