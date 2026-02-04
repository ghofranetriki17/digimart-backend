package com.nexashop.api.controller;

import com.nexashop.api.controller.role.RoleController;
import com.nexashop.api.dto.request.role.CloneRoleRequest;
import com.nexashop.api.dto.request.role.UpdateRolePermissionsRequest;
import com.nexashop.api.dto.response.role.RoleResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.RolePermission;
import com.nexashop.infrastructure.persistence.jpa.PermissionJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.RolePermissionJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoleControllerTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listRolesForbiddenWhenTenantMismatch() {
        RoleJpaRepository roleRepo = Mockito.mock(RoleJpaRepository.class);
        RolePermissionJpaRepository rolePermRepo = Mockito.mock(RolePermissionJpaRepository.class);
        PermissionJpaRepository permRepo = Mockito.mock(PermissionJpaRepository.class);
        RoleController controller = new RoleController(
                roleRepo,
                rolePermRepo,
                permRepo,
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );

        setAuth(1L, "USER");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.listRoles(2L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cloneRoleRejectsNonTemplate() {
        RoleJpaRepository roleRepo = Mockito.mock(RoleJpaRepository.class);
        RolePermissionJpaRepository rolePermRepo = Mockito.mock(RolePermissionJpaRepository.class);
        PermissionJpaRepository permRepo = Mockito.mock(PermissionJpaRepository.class);
        RoleController controller = new RoleController(
                roleRepo,
                rolePermRepo,
                permRepo,
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );

        setAuth(10L, "ADMIN");
        Role role = new Role();
        role.setId(1L);
        role.setTenantId(10L);
        role.setSystemRole(false);
        when(roleRepo.findById(1L)).thenReturn(Optional.of(role));

        CloneRoleRequest request = new CloneRoleRequest();
        request.setTemplateRoleId(1L);
        request.setCode("NEW");
        request.setLabel("New Role");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.cloneRole(request)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateRolePermissionsClearsWhenEmpty() {
        RoleJpaRepository roleRepo = Mockito.mock(RoleJpaRepository.class);
        RolePermissionJpaRepository rolePermRepo = Mockito.mock(RolePermissionJpaRepository.class);
        PermissionJpaRepository permRepo = Mockito.mock(PermissionJpaRepository.class);
        RoleController controller = new RoleController(
                roleRepo,
                rolePermRepo,
                permRepo,
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );

        setAuth(1L, "ADMIN");
        Role role = new Role();
        role.setId(3L);
        role.setTenantId(1L);
        role.setSystemRole(false);
        when(roleRepo.findById(3L)).thenReturn(Optional.of(role));
        when(rolePermRepo.findByTenantIdAndRoleId(1L, 3L)).thenReturn(List.of());

        UpdateRolePermissionsRequest request = new UpdateRolePermissionsRequest();
        request.setPermissionCodes(null);

        RoleResponse response = controller.updateRolePermissions(3L, request);
        verify(rolePermRepo).deleteByTenantIdAndRoleId(1L, 3L);
        assertTrue(response.getPermissions().isEmpty());
    }

    @Test
    void listRolePermissionsReturnsCodes() {
        RoleJpaRepository roleRepo = Mockito.mock(RoleJpaRepository.class);
        RolePermissionJpaRepository rolePermRepo = Mockito.mock(RolePermissionJpaRepository.class);
        PermissionJpaRepository permRepo = Mockito.mock(PermissionJpaRepository.class);
        RoleController controller = new RoleController(
                roleRepo,
                rolePermRepo,
                permRepo,
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );

        setAuth(1L, "ADMIN");
        Role role = new Role();
        role.setId(7L);
        role.setTenantId(1L);
        when(roleRepo.findById(7L)).thenReturn(Optional.of(role));

        RolePermission link = new RolePermission();
        link.setPermissionId(9L);
        when(rolePermRepo.findByTenantIdAndRoleId(1L, 7L)).thenReturn(List.of(link));

        Permission permission = new Permission();
        permission.setId(9L);
        permission.setCode("INV_VIEW");
        when(permRepo.findById(9L)).thenReturn(Optional.of(permission));

        Set<String> result = controller.listRolePermissions(7L);
        assertTrue(result.contains("INV_VIEW"));
    }

    private void setAuth(Long tenantId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(1L, tenantId, Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }
}
