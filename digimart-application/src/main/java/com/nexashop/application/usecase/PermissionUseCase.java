package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.PermissionRepository;
import com.nexashop.domain.user.entity.Permission;
import java.util.Comparator;
import java.util.List;


public class PermissionUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final PermissionRepository permissionRepository;

    public PermissionUseCase(CurrentUserProvider currentUserProvider, PermissionRepository permissionRepository) {
        this.currentUserProvider = currentUserProvider;
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> listPermissions() {
        currentUserProvider.requireUser();
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getDomain)
                        .thenComparing(Permission::getCode))
                .toList();
    }

    public Permission createPermission(String code, String domain, String description) {
        currentUserProvider.requireSuperAdmin();
        if (permissionRepository.existsByCode(code)) {
            throw new ConflictException("Permission code already exists");
        }
        Permission permission = new Permission();
        permission.setCode(code);
        permission.setDomain(domain);
        permission.setDescription(description);
        return permissionRepository.save(permission);
    }
}


