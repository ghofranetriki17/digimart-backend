package com.nexashop.api.dto.request.auth;

import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterTenantRequest {

    @NotBlank
    private String tenantName;

    @Email
    @NotBlank
    private String contactEmail;

    @NotBlank
    private String contactPhone;

    @NotNull
    private TenantStatus status;

    @NotNull
    private Locale defaultLocale;

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
