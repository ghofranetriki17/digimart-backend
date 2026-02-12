package com.nexashop.api.dto.response.product;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductDescriptionAiResponse {
    private Long productId;
    private String suggestion;
}
