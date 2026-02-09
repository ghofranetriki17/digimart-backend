package com.nexashop.api.dto.request.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDescriptionAiRequest {

    private String language;

    private Integer maxSentences;

    private String tone;
}
