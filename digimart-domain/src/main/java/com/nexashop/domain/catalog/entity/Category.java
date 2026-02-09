package com.nexashop.domain.catalog.entity;

import com.nexashop.domain.common.TenantEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category extends TenantEntity {

    private String name;

    private String slug;

    private String description;

    private Long parentCategoryId;

    private Integer displayOrder = 0;

    private boolean active = true;

    private Long createdBy;

    private Long updatedBy;
}
