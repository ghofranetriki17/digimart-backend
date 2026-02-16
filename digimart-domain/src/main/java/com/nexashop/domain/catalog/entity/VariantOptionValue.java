package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariantOptionValue extends TenantEntity {

    private Long variantId;

    private Long optionValueId;
}
