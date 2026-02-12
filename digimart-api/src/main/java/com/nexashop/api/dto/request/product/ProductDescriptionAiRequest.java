package com.nexashop.api.dto.request.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDescriptionAiRequest {

    private String language;

    private Integer maxSentences;

    private String tone;
}
