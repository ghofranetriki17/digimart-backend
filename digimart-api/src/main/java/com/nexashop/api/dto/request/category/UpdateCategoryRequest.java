package com.nexashop.api.dto.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String slug;

    private String description;

    private Long parentCategoryId;

    private Integer displayOrder;

    private Boolean active;
}
