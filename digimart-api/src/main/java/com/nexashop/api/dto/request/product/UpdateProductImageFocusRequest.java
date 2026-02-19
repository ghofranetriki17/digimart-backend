package com.nexashop.api.dto.request.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProductImageFocusRequest {

    @NotNull
    @Min(0)
    @Max(100)
    private Integer focusX;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer focusY;
}
