package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductOptionValue;
import java.util.List;

public interface ProductOptionValueRepository extends CrudRepositoryPort<ProductOptionValue, Long> {

    List<ProductOptionValue> findByOptionId(Long optionId);

    List<ProductOptionValue> findByOptionIds(List<Long> optionIds);

    void deleteByOptionId(Long optionId);

    void deleteByOptionIds(List<Long> optionIds);
}
