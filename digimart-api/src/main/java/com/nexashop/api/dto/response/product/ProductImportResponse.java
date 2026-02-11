package com.nexashop.api.dto.response.product;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImportResponse {

    private int totalRows;
    private int imported;
    private int failed;
    private List<ProductImportErrorResponse> errors;
}
