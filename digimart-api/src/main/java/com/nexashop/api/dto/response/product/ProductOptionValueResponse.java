package com.nexashop.api.dto.response.product;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOptionValueResponse {

    private Long id;
    private Long optionId;
    private String value;
    private String hexColor;
    private Integer displayOrder;
    private Long createdBy;
    private LocalDateTime createdAt;
}
