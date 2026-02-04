package com.nexashop.api.dto.response.sector;

import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivitySectorTenantResponse {

    private Long id;
    private String name;
    private String subdomain;
    private String contactEmail;
    private String contactPhone;
    private TenantStatus status;
    private Locale defaultLocale;
}
