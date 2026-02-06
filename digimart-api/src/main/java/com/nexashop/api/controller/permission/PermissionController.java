package com.nexashop.api.controller.permission;

import com.nexashop.api.dto.request.permission.CreatePermissionRequest;
import com.nexashop.api.dto.response.permission.PermissionResponse;
import com.nexashop.application.usecase.PermissionUseCase;
import com.nexashop.domain.user.entity.Permission;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionUseCase permissionUseCase;

    public PermissionController(PermissionUseCase permissionUseCase) {
        this.permissionUseCase = permissionUseCase;
    }

    @GetMapping
    public List<PermissionResponse> listPermissions() {
        return permissionUseCase.listPermissions().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PermissionResponse createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        Permission permission = permissionUseCase.createPermission(
                request.getCode(),
                request.getDomain(),
                request.getDescription()
        );
        return toResponse(permission);
    }

    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .domain(permission.getDomain())
                .description(permission.getDescription())
                .build();
    }
}


