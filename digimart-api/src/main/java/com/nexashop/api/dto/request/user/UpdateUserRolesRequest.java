package com.nexashop.api.dto.request.user;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRolesRequest {

    private Set<String> roles;
}
