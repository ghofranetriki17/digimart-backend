package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.billing.entity.PremiumFeature;
import com.nexashop.domain.billing.enums.FeatureCategory;
import java.util.List;
import java.util.Optional;

public interface PremiumFeatureRepository extends CrudRepositoryPort<PremiumFeature, Long> {

    Optional<PremiumFeature> findByCode(String code);

    List<PremiumFeature> findByCategoryOrderByDisplayOrderAsc(FeatureCategory category);

    List<PremiumFeature> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(FeatureCategory category);

    List<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc();

    PageResult<PremiumFeature> findByCategoryOrderByDisplayOrderAsc(PageRequest request, FeatureCategory category);

    PageResult<PremiumFeature> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(PageRequest request, FeatureCategory category);

    PageResult<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc(PageRequest request);

    PageResult<PremiumFeature> findAll(PageRequest request);
}
