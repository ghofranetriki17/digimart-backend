package com.nexashop.api.dto.request.product;

import com.nexashop.domain.catalog.entity.ProductStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductBulkStatusUpdateRequest {

    @NotEmpty
    private List<Long> productIds;

    @NotNull
    private ProductStatus status;
}
