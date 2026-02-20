package com.nexashop.application.usecase;

import com.nexashop.application.exception.ForbiddenException;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PermissionRepository;
import com.nexashop.application.port.out.RolePermissionRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.domain.user.entity.RolePermission;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationUseCase {

    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final CurrentUserProvider currentUserProvider;
    private final UserRoleAssignmentRepository userRoleAssignmentRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AuthorizationUseCase(
            CurrentUserProvider currentUserProvider,
            UserRoleAssignmentRepository userRoleAssignmentRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    public void requirePermission(String permissionCode) {
        if (hasPermission(permissionCode)) {
            return;
        }
        throw new ForbiddenException("Missing permission: " + normalizePermissionCode(permissionCode));
    }

    public boolean hasPermission(String permissionCode) {
        String normalized = normalizePermissionCode(permissionCode);
        if (normalized.isBlank()) {
            return false;
        }
        CurrentUser currentUser = currentUserProvider.requireUser();
        if (currentUser.hasRole(SUPER_ADMIN_ROLE_CODE)) {
            return true;
        }
        return resolvePermissionCodes(currentUser).contains(normalized);
    }

    public Set<String> resolveCurrentUserPermissionCodes() {
        CurrentUser currentUser = currentUserProvider.requireUser();
        return resolvePermissionCodes(currentUser);
    }

    private Set<String> resolvePermissionCodes(CurrentUser currentUser) {
        if (currentUser.hasRole(SUPER_ADMIN_ROLE_CODE)) {
            return permissionRepository.findAll().stream()
                    .map(Permission::getCode)
                    .map(this::normalizePermissionCode)
                    .filter(code -> !code.isBlank())
                    .collect(Collectors.toSet());
        }
        if (currentUser.tenantId() == null || currentUser.userId() == null) {
            return Set.of();
        }
        Set<Long> roleIds = userRoleAssignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(currentUser.tenantId(), currentUser.userId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        Set<Long> permissionIds = rolePermissionRepository
                .findByTenantIdAndRoleIdIn(currentUser.tenantId(), roleIds)
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());
        if (permissionIds.isEmpty()) {
            return Set.of();
        }
        return permissionRepository.findByIdIn(permissionIds).stream()
                .map(Permission::getCode)
                .map(this::normalizePermissionCode)
                .filter(code -> !code.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizePermissionCode(String permissionCode) {
        return permissionCode == null ? "" : permissionCode.trim().toUpperCase(Locale.ROOT);
    }
}
