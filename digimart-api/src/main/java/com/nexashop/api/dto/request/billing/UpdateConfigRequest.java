package com.nexashop.api.dto.request.billing;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateConfigRequest {

    @NotBlank
    private String configValue;

    private String description;
}
