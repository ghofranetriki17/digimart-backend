package com.nexashop.api.dto.request.billing;

import com.nexashop.domain.billing.enums.FeatureCategory;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePremiumFeatureRequest {
    private String code;
    private String name;
    private String description;
    private FeatureCategory category;
    private Boolean active;
    private Integer displayOrder;
}
