package com.nexashop.api.dto.response.product;

import com.nexashop.domain.catalog.entity.OptionType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductOptionResponse {

    private Long id;
    private Long productId;
    private String name;
    private OptionType type;
    private boolean required;
    private boolean usedForVariants;
    private Integer displayOrder;
    private Long createdBy;
    private LocalDateTime createdAt;
    private List<ProductOptionValueResponse> values;
}
