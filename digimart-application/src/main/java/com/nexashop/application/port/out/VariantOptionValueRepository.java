package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.VariantOptionValue;
import java.util.List;

public interface VariantOptionValueRepository extends CrudRepositoryPort<VariantOptionValue, Long> {

    List<VariantOptionValue> findByVariantId(Long variantId);

    List<VariantOptionValue> findByVariantIds(List<Long> variantIds);

    void deleteByVariantId(Long variantId);

    void deleteByVariantIds(List<Long> variantIds);
}
