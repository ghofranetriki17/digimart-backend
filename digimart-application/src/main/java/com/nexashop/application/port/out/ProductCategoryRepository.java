package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductCategory;
import java.util.List;

public interface ProductCategoryRepository extends CrudRepositoryPort<ProductCategory, Long> {

    List<ProductCategory> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
