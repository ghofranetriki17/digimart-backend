package com.nexashop.api.dto.request.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePermissionRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String domain;

    private String description;
}
