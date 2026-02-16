package com.nexashop.api.dto.request.product;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductVariantsRequest {

    @Valid
    private List<ProductVariantRequest> variants;
}
