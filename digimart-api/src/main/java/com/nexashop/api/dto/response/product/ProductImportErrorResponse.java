package com.nexashop.api.dto.response.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImportErrorResponse {

    private int row;
    private String message;
}
