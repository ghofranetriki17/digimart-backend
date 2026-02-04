package com.nexashop.api.dto.request.sector;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateActivitySectorRequest {

    @NotBlank
    private String label;

    @NotBlank
    private String description;

    private Boolean active;
}
