package com.nexashop.api.dto.request.product;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductBulkDeleteRequest {

    @NotEmpty
    private List<Long> productIds;
}
