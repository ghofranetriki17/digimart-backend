package com.nexashop.api.controller.role;

import com.nexashop.api.dto.request.role.CloneRoleRequest;
import com.nexashop.api.dto.request.role.CreateRoleRequest;
import com.nexashop.api.dto.request.role.CreateRoleTemplateRequest;
import com.nexashop.api.dto.request.role.UpdateRolePermissionsRequest;
import com.nexashop.api.dto.request.role.UpdateRoleRequest;
import com.nexashop.api.dto.response.role.RoleResponse;
import com.nexashop.application.usecase.RoleUseCase;
import com.nexashop.domain.user.entity.Role;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleUseCase roleUseCase;

    public RoleController(
            RoleUseCase roleUseCase
    ) {
        this.roleUseCase = roleUseCase;
    }

    @GetMapping
    public List<RoleResponse> listRoles(@RequestParam Long tenantId) {
        return roleUseCase.listRoles(tenantId).stream()
                .map(this::toResponseWithPermissions)
                .collect(Collectors.toList());
    }

    @PostMapping("/clone")
    public RoleResponse cloneRole(@Valid @RequestBody CloneRoleRequest request) {
        RoleUseCase.RoleDetails details = roleUseCase.cloneRole(
                request.getTemplateRoleId(),
                request.getCode(),
                request.getLabel(),
                request.getTargetTenantId()
        );
        return toResponseWithPermissions(details);
    }

    @PostMapping
    public RoleResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleUseCase.RoleDetails details = roleUseCase.createRole(
                request.getCode(),
                request.getLabel(),
                request.getTargetTenantId()
        );
        return toResponseWithPermissions(details);
    }

    @PostMapping("/templates")
    public RoleResponse createTemplate(@Valid @RequestBody CreateRoleTemplateRequest request) {
        RoleUseCase.RoleDetails details = roleUseCase.createTemplate(
                request.getCode(),
                request.getLabel()
        );
        return toResponseWithPermissions(details);
    }

    @PutMapping("/{id}")
    public RoleResponse updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        RoleUseCase.RoleDetails details = roleUseCase.updateRole(id, request.getLabel());
        return toResponseWithPermissions(details);
    }

    @PutMapping("/{id}/permissions")
    @Transactional
    public RoleResponse updateRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        RoleUseCase.RoleDetails details = roleUseCase.updateRolePermissions(
                id,
                request.getPermissionCodes()
        );
        return toResponseWithPermissions(details);
    }

    @GetMapping("/{id}/permissions")
    public Set<String> listRolePermissions(@PathVariable Long id) {
        return roleUseCase.listRolePermissions(id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteRole(@PathVariable Long id) {
        roleUseCase.deleteRole(id);
    }

    private RoleResponse toResponseWithPermissions(RoleUseCase.RoleDetails details) {
        Role role = details.role();
        Set<String> permissions = details.permissions();
        return RoleResponse.builder()
                .id(role.getId())
                .tenantId(role.getTenantId())
                .code(role.getCode())
                .label(role.getLabel())
                .systemRole(role.isSystemRole())
                .permissions(permissions)
                .build();
    }
}


