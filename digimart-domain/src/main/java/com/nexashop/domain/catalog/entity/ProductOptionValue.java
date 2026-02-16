package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOptionValue extends TenantEntity {

    private Long optionId;

    private String value;

    private String hexColor;

    private Integer displayOrder = 0;

    private Long createdBy;
}
