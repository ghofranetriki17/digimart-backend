package com.nexashop.api.dto.request.product;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductInventoryRequest {

    @NotNull
    private Long storeId;

    private Integer quantity;

    private Integer lowStockThreshold;

    private Boolean activeInStore;
}
