package com.nexashop.api.dto.response.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductBulkActionResponse {

    private int affected;
}
