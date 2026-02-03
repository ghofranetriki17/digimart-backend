package com.nexashop.api.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloneRoleRequest {

    @NotNull
    private Long templateRoleId;

    @NotBlank
    private String code;

    @NotBlank
    private String label;
}
