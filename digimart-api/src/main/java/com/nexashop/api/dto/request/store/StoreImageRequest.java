package com.nexashop.api.dto.request.store;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreImageRequest {

    @NotBlank
    private String imageUrl;
}
