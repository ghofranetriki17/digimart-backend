package com.nexashop.api.dto.request.tenant;

import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTenantRequest {

    @NotBlank
    private String name;

    private String contactEmail;

    private String contactPhone;

    private String logoUrl;

    @NotNull
    private TenantStatus status;

    @NotNull
    private Locale defaultLocale;

    private Long sectorId;
}
