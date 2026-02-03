package com.nexashop.api.controller;

import com.nexashop.api.controller.permission.PermissionController;
import com.nexashop.api.dto.request.permission.CreatePermissionRequest;
import com.nexashop.api.dto.response.permission.PermissionResponse;
import com.nexashop.api.security.AuthenticatedUser;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.infrastructure.persistence.jpa.PermissionJpaRepository;
import java.util.List;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class PermissionControllerTest {

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listPermissionsRequiresAuth() {
        PermissionJpaRepository repo = Mockito.mock(PermissionJpaRepository.class);
        PermissionController controller = new PermissionController(repo);
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, controller::listPermissions);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void createPermissionConflictsOnDuplicateCode() {
        PermissionJpaRepository repo = Mockito.mock(PermissionJpaRepository.class);
        PermissionController controller = new PermissionController(repo);
        setAuth(1L, "SUPER_ADMIN");
        when(repo.existsByCode("INV_VIEW")).thenReturn(true);

        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("INV_VIEW");
        request.setDomain("inventory");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.createPermission(request)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void listPermissionsSortedByDomainThenCode() {
        PermissionJpaRepository repo = Mockito.mock(PermissionJpaRepository.class);
        PermissionController controller = new PermissionController(repo);
        setAuth(1L, "ADMIN");

        Permission a = new Permission();
        a.setId(1L);
        a.setDomain("sales");
        a.setCode("B");
        Permission b = new Permission();
        b.setId(2L);
        b.setDomain("inventory");
        b.setCode("A");

        when(repo.findAll()).thenReturn(List.of(a, b));

        List<PermissionResponse> responses = controller.listPermissions();
        assertEquals(2, responses.size());
        assertTrue("inventory".equals(responses.get(0).getDomain()));
    }

    private void setAuth(Long tenantId, String... roles) {
        AuthenticatedUser user = new AuthenticatedUser(1L, tenantId, Set.of(roles));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );
    }
}
