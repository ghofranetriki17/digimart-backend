package com.nexashop.api.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiTextRequest {

    @NotBlank
    private String prompt;
}
