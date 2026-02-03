package com.nexashop.api.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTenantStep2Request {

    @NotNull
    private Long tenantId;

    @Email
    @NotBlank
    private String ownerEmail;

    @NotBlank
    private String ownerPassword;

    @NotBlank
    private String ownerFirstName;

    @NotBlank
    private String ownerLastName;
}
