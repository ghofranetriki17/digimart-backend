package com.nexashop.api.dto.request.role;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRolePermissionsRequest {

    private Set<String> permissionCodes;
}
