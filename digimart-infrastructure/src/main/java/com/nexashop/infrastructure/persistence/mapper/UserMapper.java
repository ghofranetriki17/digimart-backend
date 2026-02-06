package com.nexashop.infrastructure.persistence.mapper;

import com.nexashop.domain.user.entity.Permission;
import com.nexashop.domain.user.entity.RefreshToken;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.RolePermission;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.model.user.PermissionJpaEntity;
import com.nexashop.infrastructure.persistence.model.user.RefreshTokenJpaEntity;
import com.nexashop.infrastructure.persistence.model.user.RoleJpaEntity;
import com.nexashop.infrastructure.persistence.model.user.RolePermissionJpaEntity;
import com.nexashop.infrastructure.persistence.model.user.UserJpaEntity;
import com.nexashop.infrastructure.persistence.model.user.UserRoleAssignmentJpaEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        User domain = new User();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setEmail(entity.getEmail());
        domain.setPasswordHash(entity.getPasswordHash());
        domain.setFirstName(entity.getFirstName());
        domain.setLastName(entity.getLastName());
        domain.setPhone(entity.getPhone());
        domain.setImageUrl(entity.getImageUrl());
        domain.setEnabled(entity.isEnabled());
        domain.setLastLogin(entity.getLastLogin());
        return domain;
    }

    public static UserJpaEntity toJpa(User domain) {
        if (domain == null) {
            return null;
        }
        UserJpaEntity entity = new UserJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setFirstName(domain.getFirstName());
        entity.setLastName(domain.getLastName());
        entity.setPhone(domain.getPhone());
        entity.setImageUrl(domain.getImageUrl());
        entity.setEnabled(domain.isEnabled());
        entity.setLastLogin(domain.getLastLogin());
        return entity;
    }

    public static Role toDomain(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Role domain = new Role();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setCode(entity.getCode());
        domain.setLabel(entity.getLabel());
        domain.setSystemRole(entity.isSystemRole());
        return domain;
    }

    public static RoleJpaEntity toJpa(Role domain) {
        if (domain == null) {
            return null;
        }
        RoleJpaEntity entity = new RoleJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setCode(domain.getCode());
        entity.setLabel(domain.getLabel());
        entity.setSystemRole(domain.isSystemRole());
        return entity;
    }

    public static Permission toDomain(PermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Permission domain = new Permission();
        MapperUtils.mapBaseToDomain(entity, domain);
        domain.setCode(entity.getCode());
        domain.setDomain(entity.getDomain());
        domain.setDescription(entity.getDescription());
        return domain;
    }

    public static PermissionJpaEntity toJpa(Permission domain) {
        if (domain == null) {
            return null;
        }
        PermissionJpaEntity entity = new PermissionJpaEntity();
        MapperUtils.mapBaseToJpa(domain, entity);
        entity.setCode(domain.getCode());
        entity.setDomain(domain.getDomain());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    public static UserRoleAssignment toDomain(UserRoleAssignmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        UserRoleAssignment domain = new UserRoleAssignment();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setUserId(entity.getUserId());
        domain.setRoleId(entity.getRoleId());
        domain.setActive(entity.isActive());
        return domain;
    }

    public static UserRoleAssignmentJpaEntity toJpa(UserRoleAssignment domain) {
        if (domain == null) {
            return null;
        }
        UserRoleAssignmentJpaEntity entity = new UserRoleAssignmentJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setUserId(domain.getUserId());
        entity.setRoleId(domain.getRoleId());
        entity.setActive(domain.isActive());
        return entity;
    }

    public static RolePermission toDomain(RolePermissionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        RolePermission domain = new RolePermission();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setRoleId(entity.getRoleId());
        domain.setPermissionId(entity.getPermissionId());
        return domain;
    }

    public static RolePermissionJpaEntity toJpa(RolePermission domain) {
        if (domain == null) {
            return null;
        }
        RolePermissionJpaEntity entity = new RolePermissionJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setRoleId(domain.getRoleId());
        entity.setPermissionId(domain.getPermissionId());
        return entity;
    }

    public static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        RefreshToken domain = new RefreshToken();
        MapperUtils.mapTenantToDomain(entity, domain);
        domain.setUserId(entity.getUserId());
        domain.setTokenHash(entity.getTokenHash());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setRevokedAt(entity.getRevokedAt());
        domain.setDeviceInfo(entity.getDeviceInfo());
        return domain;
    }

    public static RefreshTokenJpaEntity toJpa(RefreshToken domain) {
        if (domain == null) {
            return null;
        }
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        MapperUtils.mapTenantToJpa(domain, entity);
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevokedAt(domain.getRevokedAt());
        entity.setDeviceInfo(domain.getDeviceInfo());
        return entity;
    }
}
