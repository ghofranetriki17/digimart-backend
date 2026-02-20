package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.RolePermissionRepository;
import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.application.port.out.PermissionRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.RolePermission;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;


public class RoleUseCase {

    public record RoleDetails(Role role, Set<String> permissions) {}

    private static final Long TEMPLATE_TENANT_ID = 0L;
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final CurrentUserProvider currentUserProvider;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;

    public RoleUseCase(
            CurrentUserProvider currentUserProvider,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository,
            UserRoleAssignmentRepository userRoleAssignmentRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    public List<RoleDetails> listRoles(Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        Set<Long> tenantIds = Set.of(TEMPLATE_TENANT_ID, tenantId);
        return roleRepository.findByTenantIdIn(tenantIds).stream()
                .map(this::toDetails)
                .collect(Collectors.toList());
    }

    public RoleDetails cloneRole(
            Long templateRoleId,
            String code,
            String label,
            Long targetTenantId
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = requesterTenantId;
        if (isSuperAdmin && targetTenantId != null) {
            tenantId = targetTenantId;
        }

        Role template = roleRepository.findById(templateRoleId)
                .orElseThrow(() -> new NotFoundException("Template role not found"));
        if (!template.isSystemRole() || !TEMPLATE_TENANT_ID.equals(template.getTenantId())) {
            throw new ForbiddenException("Role is not a template");
        }
        ensureRoleCodeIsNotReserved(code);

        if (roleRepository.findByTenantIdAndCode(tenantId, code).isPresent()) {
            throw new ConflictException("Role code already exists");
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(code);
        role.setLabel(label);
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

        return toDetails(saved);
    }

    public RoleDetails createRole(
            String code,
            String label,
            Long targetTenantId
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = requesterTenantId;
        if (isSuperAdmin && targetTenantId != null) {
            tenantId = targetTenantId;
        }
        if (TEMPLATE_TENANT_ID.equals(tenantId)) {
            throw new ForbiddenException("Use /api/roles/templates for templates");
        }
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        ensureRoleCodeIsNotReserved(code);
        if (roleRepository.findByTenantIdAndCode(tenantId, code).isPresent()) {
            throw new ConflictException("Role code already exists");
        }

        Role role = new Role();
        role.setTenantId(tenantId);
        role.setCode(code);
        role.setLabel(label);
        role.setSystemRole(false);
        Role saved = roleRepository.save(role);

        return toDetails(saved);
    }

    public RoleDetails createTemplate(String code, String label) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        boolean hasPlatformAdmin = currentUser.hasRole("SUPER_ADMIN") || currentUser.tenantId() == 1L;
        if (!hasPlatformAdmin) {
            throw new ForbiddenException("Platform admin access required");
        }
        ensureRoleCodeIsNotReserved(code);
        if (roleRepository.findByTenantIdAndCode(TEMPLATE_TENANT_ID, code).isPresent()) {
            throw new ConflictException("Role code already exists");
        }

        Role role = new Role();
        role.setTenantId(TEMPLATE_TENANT_ID);
        role.setCode(code);
        role.setLabel(label);
        role.setSystemRole(true);
        Role saved = roleRepository.save(role);

        return toDetails(saved);
    }

    public RoleDetails updateRole(Long id, String label) {
        currentUserProvider.requireUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        role.setLabel(label);
        Role saved = roleRepository.save(role);
        return toDetails(saved);
    }

    public RoleDetails updateRolePermissions(Long id, Set<String> permissionCodes) {
        currentUserProvider.requireUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        Set<String> codes = permissionCodes == null
                ? Set.of()
                : permissionCodes.stream()
                        .filter(code -> code != null && !code.isBlank())
                        .collect(Collectors.toSet());

        List<Permission> permissions = codes.isEmpty()
                ? List.of()
                : permissionRepository.findByCodeIn(codes);

        rolePermissionRepository.deleteByTenantIdAndRoleId(role.getTenantId(), role.getId());
        rolePermissionRepository.flush();

        for (Permission permission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setTenantId(role.getTenantId());
            rolePermission.setRoleId(role.getId());
            rolePermission.setPermissionId(permission.getId());
            rolePermissionRepository.save(rolePermission);
        }
        rolePermissionRepository.flush();

        return toDetails(role);
    }

    public Set<String> listRolePermissions(Long id) {
        currentUserProvider.requireUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        return rolePermissionRepository.findByTenantIdAndRoleId(role.getTenantId(), role.getId()).stream()
                .map(RolePermission::getPermissionId)
                .map(permissionRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
    }

    public void deleteRole(Long id) {
        currentUserProvider.requireUser();
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        rolePermissionRepository.deleteByRoleId(role.getId());
        userRoleAssignmentRepository.deleteByRoleId(role.getId());
        roleRepository.delete(role);
    }

    private RoleDetails toDetails(Role role) {
        Set<String> permissions = rolePermissionRepository
                .findByTenantIdAndRoleId(role.getTenantId(), role.getId()).stream()
                .map(RolePermission::getPermissionId)
                .map(permissionRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        return new RoleDetails(role, permissions);
    }

    private void ensureRoleCodeIsNotReserved(String code) {
        if (SUPER_ADMIN_ROLE_CODE.equals(normalizeRoleCode(code))) {
            throw new ForbiddenException("Role code SUPER_ADMIN is reserved");
        }
    }

    private String normalizeRoleCode(String code) {
        return code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }
}


