package com.nexashop.api.dto.response.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryDescriptionAiResponse {
    private Long categoryId;
    private String suggestion;
}
