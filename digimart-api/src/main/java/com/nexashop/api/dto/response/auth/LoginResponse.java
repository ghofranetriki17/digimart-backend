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
    private Set<String> roles;
}
