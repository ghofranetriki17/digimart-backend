package com.nexashop.api.dto.response.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterTenantStep1Response {

    private Long tenantId;
    private String subdomain;
}
