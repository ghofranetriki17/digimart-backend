package com.nexashop.api.dto.request.billing;

import com.nexashop.domain.billing.enums.FeatureCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePremiumFeatureRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private FeatureCategory category;

    private boolean active = true;

    private Integer displayOrder = 0;
}
