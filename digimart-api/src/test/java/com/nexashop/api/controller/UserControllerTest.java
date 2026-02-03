package com.nexashop.api.controller;

import com.nexashop.api.controller.user.UserController;
import com.nexashop.api.dto.request.user.UpdateUserRolesRequest;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.domain.user.entity.User;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listUsersForbiddenWhenTenantMismatch() {
        UserController controller = new UserController(
                Mockito.mock(TenantJpaRepository.class),
                Mockito.mock(UserJpaRepository.class),
                Mockito.mock(RoleJpaRepository.class),
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );
        setAuth(1L, "USER");
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.listUsers(2L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateUserRolesRejectsInvalidRoleCodes() {
        UserJpaRepository userRepo = Mockito.mock(UserJpaRepository.class);
        RoleJpaRepository roleRepo = Mockito.mock(RoleJpaRepository.class);
        UserRoleAssignmentJpaRepository assignmentRepo = Mockito.mock(UserRoleAssignmentJpaRepository.class);
        UserController controller = new UserController(
                Mockito.mock(TenantJpaRepository.class),
                userRepo,
                roleRepo,
                assignmentRepo
        );

        setAuth(5L, "ADMIN");
        User user = new User();
        user.setId(1L);
        user.setTenantId(5L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepo.findByTenantIdAndCodeIn(5L, Set.of("ADMIN", "OWNER")))
                .thenReturn(java.util.List.of());

        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of("ADMIN", "OWNER"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.updateUserRoles(1L, request)
        );
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getUserForbiddenAcrossTenants() {
        UserJpaRepository userRepo = Mockito.mock(UserJpaRepository.class);
        UserController controller = new UserController(
                Mockito.mock(TenantJpaRepository.class),
                userRepo,
                Mockito.mock(RoleJpaRepository.class),
                Mockito.mock(UserRoleAssignmentJpaRepository.class)
        );
        setAuth(1L, "USER");
        User user = new User();
        user.setId(10L);
        user.setTenantId(2L);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.getUser(10L)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private void setAuth(Long tenantId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(1L, tenantId, Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }
}
