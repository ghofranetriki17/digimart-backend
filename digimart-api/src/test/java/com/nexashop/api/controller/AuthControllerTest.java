package com.nexashop.api.controller;

import com.nexashop.api.controller.auth.AuthController;
import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.request.auth.RegisterTenantStep1Request;
import com.nexashop.api.dto.response.auth.RegisterTenantStep1Response;
import com.nexashop.application.usecase.AuthUseCase;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import jakarta.servlet.http.HttpServletRequest;
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
        AuthUseCase authUseCase = Mockito.mock(AuthUseCase.class);
        AuthController controller = new AuthController(authUseCase);

        LoginRequest request = new LoginRequest();
        request.setEmail("no@user.com");
        request.setPassword("pass");

        when(authUseCase.login(
                request.getEmail(),
                request.getPassword(),
                null
        )).thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.login(request, Mockito.mock(HttpServletRequest.class))
        );
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void registerTenantStep1ReturnsResult() {
        AuthUseCase authUseCase = Mockito.mock(AuthUseCase.class);
        AuthController controller = new AuthController(authUseCase);

        RegisterTenantStep1Request request = new RegisterTenantStep1Request();
        request.setTenantName("My Store");
        request.setContactEmail("contact@demo.com");
        request.setContactPhone("123");
        request.setStatus(TenantStatus.ACTIVE);
        request.setDefaultLocale(Locale.FR);

        when(authUseCase.registerTenantStep1(
                request.getTenantName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getLogoUrl(),
                request.getStatus(),
                request.getDefaultLocale(),
                request.getSectorId()
        )).thenReturn(new AuthUseCase.RegisterTenantStep1Result(99L, "my-store"));

        RegisterTenantStep1Response response = controller.registerTenantStep1(request);
        assertEquals(99L, response.getTenantId());
        assertEquals("my-store", response.getSubdomain());
    }
}
