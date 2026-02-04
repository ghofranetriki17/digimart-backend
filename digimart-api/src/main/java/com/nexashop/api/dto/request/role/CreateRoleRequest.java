package com.nexashop.api.dto.request.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoleRequest {

    private Long targetTenantId;

    @NotBlank
    private String code;

    @NotBlank
    private String label;
}
