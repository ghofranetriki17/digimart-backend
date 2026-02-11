package com.nexashop.api.dto.response.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductStoreRefResponse {

    private Long id;
    private String name;
}
