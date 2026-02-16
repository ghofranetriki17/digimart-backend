package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductOption;
import java.util.List;

public interface ProductOptionRepository extends CrudRepositoryPort<ProductOption, Long> {

    List<ProductOption> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
