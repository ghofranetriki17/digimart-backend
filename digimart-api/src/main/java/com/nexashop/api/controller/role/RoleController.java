package com.nexashop.api.controller.role;

import com.nexashop.api.dto.request.role.CloneRoleRequest;
import com.nexashop.api.dto.request.role.UpdateRolePermissionsRequest;
import com.nexashop.api.dto.request.role.UpdateRoleRequest;
import com.nexashop.api.dto.response.role.RoleResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.RolePermission;
import com.nexashop.infrastructure.persistence.jpa.PermissionJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.RolePermissionJpaRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private static final Long TEMPLATE_TENANT_ID = 0L;

    private final RoleJpaRepository roleRepository;
    private final RolePermissionJpaRepository rolePermissionRepository;
    private final PermissionJpaRepository permissionRepository;

    public RoleController(
            RoleJpaRepository roleRepository,
            RolePermissionJpaRepository rolePermissionRepository,
            PermissionJpaRepository permissionRepository
    ) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<RoleResponse> listRoles(@RequestParam Long tenantId) {
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        Set<Long> tenantIds = Set.of(TEMPLATE_TENANT_ID, tenantId);
        return roleRepository.findByTenantIdIn(tenantIds).stream()
                .map(this::toResponseWithPermissions)
                .collect(Collectors.toList());
    }

    @PostMapping("/clone")
    public RoleResponse cloneRole(@Valid @RequestBody CloneRoleRequest request) {
        AuthenticatedUser user = SecurityContextUtil.requireUser();
        Long tenantId = user.getTenantId();

        Role template = roleRepository.findById(request.getTemplateRoleId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Template role not found"));
        if (!template.isSystemRole() || !TEMPLATE_TENANT_ID.equals(template.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Role is not a template");
        }

        if (roleRepository.findByTenantIdAndCode(tenantId, request.getCode()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Role code already exists");
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(request.getCode());
        role.setLabel(request.getLabel());
        role.setSystemRole(false);
        Role saved = roleRepository.save(role);

        List<RolePermission> templatePerms =
                rolePermissionRepository.findByTenantIdAndRoleId(TEMPLATE_TENANT_ID, template.getId());
        for (RolePermission templatePerm : templatePerms) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setTenantId(tenantId);
            rolePermission.setRoleId(saved.getId());
            rolePermission.setPermissionId(templatePerm.getPermissionId());
            rolePermissionRepository.save(rolePermission);
        }

        return toResponseWithPermissions(saved);
    }

    @PutMapping("/{id}")
    public RoleResponse updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found"));
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !role.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (TEMPLATE_TENANT_ID.equals(role.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Template roles cannot be modified");
        }
        role.setLabel(request.getLabel());
        return toResponseWithPermissions(roleRepository.save(role));
    }

    @PutMapping("/{id}/permissions")
    public RoleResponse updateRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found"));
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !role.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (TEMPLATE_TENANT_ID.equals(role.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Template roles cannot be modified");
        }

        Set<String> codes = request.getPermissionCodes() == null
                ? Set.of()
                : request.getPermissionCodes().stream()
                        .filter(code -> code != null && !code.isBlank())
                        .collect(Collectors.toSet());

        List<Permission> permissions = codes.isEmpty()
                ? List.of()
                : permissionRepository.findByCodeIn(codes);

        rolePermissionRepository.deleteByTenantIdAndRoleId(role.getTenantId(), role.getId());

        for (Permission permission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setTenantId(role.getTenantId());
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermissionRepository.save(rolePermission);
        }

        return toResponseWithPermissions(role);
    }

    @GetMapping("/{id}/permissions")
    public Set<String> listRolePermissions(@PathVariable Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found"));
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !role.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return rolePermissionRepository.findByTenantIdAndRoleId(role.getTenantId(), role.getId()).stream()
                .map(RolePermission::getPermissionId)
                .map(permissionId -> permissionRepository.findById(permissionId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    private RoleResponse toResponseWithPermissions(Role role) {
        Set<String> permissions = rolePermissionRepository
                .findByTenantIdAndRoleId(role.getTenantId(), role.getId()).stream()
                .map(RolePermission::getPermissionId)
                .map(permissionId -> permissionRepository.findById(permissionId))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getCode)
                .collect(Collectors.toSet());

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
