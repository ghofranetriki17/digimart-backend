package com.nexashop.api.dto.response.role;

import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private Long tenantId;
    private String code;
    private String label;
    private boolean systemRole;
    private Set<String> permissions;
}
