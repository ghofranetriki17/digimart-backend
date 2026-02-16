package com.nexashop.api.dto.response.product;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VariantInventoryResponse {

    private Long id;
    private Long variantId;
    private Long storeId;
    private Integer quantity;
    private Integer lowStockThreshold;
    private boolean activeInStore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
