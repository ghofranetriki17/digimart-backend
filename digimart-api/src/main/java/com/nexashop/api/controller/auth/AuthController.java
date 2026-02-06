package com.nexashop.api.controller.auth;

import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.request.auth.RegisterTenantStep1Request;
import com.nexashop.api.dto.request.auth.RegisterTenantStep2Request;
import com.nexashop.api.dto.response.auth.LoginResponse;
import com.nexashop.api.dto.response.auth.RegisterTenantStep1Response;
import com.nexashop.application.usecase.AuthUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthUseCase.LoginResult result = authUseCase.login(
                request.getEmail(),
                request.getPassword(),
                httpRequest.getHeader("User-Agent")
        );

        return LoginResponse.builder()
                .token(result.token())
                .userId(result.userId())
                .tenantId(result.tenantId())
                .sectorId(result.sectorId())
                .sectorLabel(result.sectorLabel())
                .roles(result.roles())
                .build();
    }

    @PostMapping("/register-tenant")
    public void registerTenantLegacy() {
        throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Use /api/auth/register-tenant/step1 then /api/auth/register-tenant/step2"
        );
    }

    @PostMapping("/register-tenant/step1")
    @Transactional
    public RegisterTenantStep1Response registerTenantStep1(
            @Valid @RequestBody RegisterTenantStep1Request request
    ) {
        AuthUseCase.RegisterTenantStep1Result result = authUseCase.registerTenantStep1(
                request.getTenantName(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getLogoUrl(),
                request.getStatus(),
                request.getDefaultLocale(),
                request.getSectorId()
        );

        return RegisterTenantStep1Response.builder()
                .tenantId(result.tenantId())
                .subdomain(result.subdomain())
                .build();
    }

    @PostMapping("/register-tenant/step2")
    @Transactional
    public LoginResponse registerTenantStep2(
            @Valid @RequestBody RegisterTenantStep2Request request,
            HttpServletRequest httpRequest
    ) {
        AuthUseCase.LoginResult result = authUseCase.registerTenantStep2(
                request.getTenantId(),
                request.getOwnerEmail(),
                request.getOwnerPassword(),
                request.getOwnerFirstName(),
                request.getOwnerLastName(),
                httpRequest.getHeader("User-Agent")
        );

        return LoginResponse.builder()
                .token(result.token())
                .userId(result.userId())
                .tenantId(result.tenantId())
                .sectorId(result.sectorId())
                .sectorLabel(result.sectorLabel())
                .roles(result.roles())
                .build();
    }
}
