package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductStoreInventory extends TenantEntity {

    private Long productId;

    private Long storeId;

    private Integer quantity = 0;

    private Integer lowStockThreshold;

    private boolean activeInStore = true;
}
