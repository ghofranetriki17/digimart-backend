package com.nexashop.api.dto.response.permission;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PermissionResponse {

    private Long id;
    private String code;
    private String domain;
    private String description;
}
