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
public class RegisterTenantStep1Request {

    @NotBlank
    private String tenantName;

    @Email
    @NotBlank
    private String contactEmail;

    @NotBlank
    private String contactPhone;

    private String logoUrl;

    @NotNull
    private TenantStatus status;

    @NotNull
    private Locale defaultLocale;
}
