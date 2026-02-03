package com.nexashop.api.dto.response.tenant;

import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TenantResponse {

    private Long id;
    private String name;
    private String subdomain;
    private String contactEmail;
    private String contactPhone;
    private String logoUrl;
    private TenantStatus status;
    private Locale defaultLocale;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
