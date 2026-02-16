package com.nexashop.application.port.out;

import com.nexashop.domain.catalog.entity.VariantStoreInventory;
import java.util.List;

public interface VariantStoreInventoryRepository extends CrudRepositoryPort<VariantStoreInventory, Long> {

    List<VariantStoreInventory> findByVariantId(Long variantId);

    List<VariantStoreInventory> findByVariantIds(List<Long> variantIds);

    List<VariantStoreInventory> findByStoreId(Long storeId);

    void deleteByVariantId(Long variantId);

    void deleteByVariantIds(List<Long> variantIds);
}
