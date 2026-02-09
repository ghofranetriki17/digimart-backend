package com.nexashop.api.dto.response.category;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {

    private Long id;
    private Long tenantId;
    private String name;
    private String slug;
    private String description;
    private Long parentCategoryId;
    private Integer displayOrder;
    private boolean active;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
