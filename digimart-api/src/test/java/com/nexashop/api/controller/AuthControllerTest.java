package com.nexashop.api.controller;

import com.nexashop.api.controller.auth.AuthController;
import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.request.auth.RegisterTenantStep1Request;
import com.nexashop.api.dto.response.auth.RegisterTenantStep1Response;
import com.nexashop.api.security.AuthTokenService;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.tenant.entity.TenantStatus;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void loginRejectsUnknownUser() {
        UserJpaRepository userRepo = Mockito.mock(UserJpaRepository.class);
        AuthController controller = new AuthController(
                userRepo,
                Mockito.mock(UserRoleAssignmentJpaRepository.class),
                Mockito.mock(RoleJpaRepository.class),
                Mockito.mock(TenantJpaRepository.class),
                Mockito.mock(AuthTokenService.class)
        );
        when(userRepo.findByEmail("no@user.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("no@user.com");
        request.setPassword("pass");

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(request, Mockito.mock(HttpServletRequest.class))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void registerTenantStep1NormalizesSubdomain() {
        TenantJpaRepository tenantRepo = Mockito.mock(TenantJpaRepository.class);
        when(tenantRepo.existsBySubdomain("my-store")).thenReturn(false);

        Tenant saved = new Tenant();
        saved.setId(99L);
        saved.setName("My Store");
        saved.setSubdomain("my-store");
        saved.setStatus(TenantStatus.ACTIVE);
        saved.setDefaultLocale(Locale.FR);
        when(tenantRepo.save(Mockito.any(Tenant.class))).thenReturn(saved);

        AuthController controller = new AuthController(
                Mockito.mock(UserJpaRepository.class),
                Mockito.mock(UserRoleAssignmentJpaRepository.class),
                Mockito.mock(RoleJpaRepository.class),
                tenantRepo,
                Mockito.mock(AuthTokenService.class)
        );

        RegisterTenantStep1Request request = new RegisterTenantStep1Request();
        request.setTenantName("My Store");
        request.setContactEmail("contact@demo.com");
        request.setContactPhone("123");
        request.setStatus(TenantStatus.ACTIVE);
        request.setDefaultLocale(Locale.FR);

        RegisterTenantStep1Response response = controller.registerTenantStep1(request);
        assertEquals(99L, response.getTenantId());
        assertEquals("my-store", response.getSubdomain());
    }
}
