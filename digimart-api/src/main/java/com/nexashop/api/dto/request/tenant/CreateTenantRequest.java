package com.nexashop.api.dto.request.tenant;

import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String subdomain;

    private String contactEmail;

    private String contactPhone;

    private String logoUrl;

    private String studioBackgroundUrl;

    @NotNull
    private TenantStatus status;

    @NotNull
    private Locale defaultLocale;

    private Long sectorId;
}
