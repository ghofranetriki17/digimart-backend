package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductImage extends TenantEntity {

    private Long productId;

    private String imageUrl;

    private String altText;

    private Integer displayOrder = 0;

    private boolean primary = false;
}
