package com.nexashop.api.dto.response.auth;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private Long userId;
    private Long tenantId;
    private Long sectorId;
    private String sectorLabel;
    private Set<String> roles;
}
