package com.nexashop.api.dto.response.ai;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiTextResponse {
    private String text;
}
