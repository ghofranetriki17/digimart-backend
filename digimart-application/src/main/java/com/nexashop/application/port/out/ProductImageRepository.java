package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductImage;
import java.util.List;

public interface ProductImageRepository extends CrudRepositoryPort<ProductImage, Long> {

    List<ProductImage> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}
