package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductOption extends TenantEntity {

    private Long productId;

    private String name;

    private OptionType type = OptionType.TEXT;

    private boolean required = false;

    private boolean usedForVariants = true;

    private Integer displayOrder = 0;

    private Long createdBy;
}
