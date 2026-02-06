package com.nexashop.domain.billing.entity;

import com.nexashop.domain.billing.enums.FeatureCategory;
import com.nexashop.domain.common.AuditableEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PremiumFeature extends AuditableEntity {

    private String code;

    private String name;

    private String description;

    private FeatureCategory category;

    private boolean active = true;

    private Integer displayOrder = 0;
}
