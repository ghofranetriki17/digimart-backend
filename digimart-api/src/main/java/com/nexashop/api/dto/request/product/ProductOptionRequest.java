package com.nexashop.api.dto.request.product;

import com.nexashop.domain.catalog.entity.OptionType;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOptionRequest {

    private String name;

    private OptionType type;

    private Boolean required;

    private Boolean usedForVariants;

    private Integer displayOrder;

    @Valid
    private List<ProductOptionValueRequest> values;
}
