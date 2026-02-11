package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCategory extends TenantEntity {

    private Long productId;

    private Long categoryId;

    private boolean primary = false;

    private Integer displayOrder = 0;

    private Long createdBy;
}
