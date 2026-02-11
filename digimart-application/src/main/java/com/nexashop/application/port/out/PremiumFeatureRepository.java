package com.nexashop.application.port.out;

import com.nexashop.application.common.PageRequest;
import com.nexashop.application.common.PageResult;
import com.nexashop.domain.billing.entity.PremiumFeature;
import java.util.List;
import java.util.Optional;

public interface PremiumFeatureRepository extends CrudRepositoryPort<PremiumFeature, Long> {

    Optional<PremiumFeature> findByCode(String code);

    List<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc();

    PageResult<PremiumFeature> findByActiveTrueOrderByDisplayOrderAsc(PageRequest request);

    PageResult<PremiumFeature> findAll(PageRequest request);
}
