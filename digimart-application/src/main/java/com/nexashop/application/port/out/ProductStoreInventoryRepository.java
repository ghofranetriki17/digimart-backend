package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.ProductStoreInventory;
import java.util.List;

public interface ProductStoreInventoryRepository extends CrudRepositoryPort<ProductStoreInventory, Long> {

    List<ProductStoreInventory> findByProductId(Long productId);

    List<ProductStoreInventory> findByStoreId(Long storeId);

    void deleteByProductId(Long productId);
}
