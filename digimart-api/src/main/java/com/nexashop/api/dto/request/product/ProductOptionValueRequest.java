package com.nexashop.api.dto.request.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOptionValueRequest {

    private String value;

    private String hexColor;

    private Integer displayOrder;
}
