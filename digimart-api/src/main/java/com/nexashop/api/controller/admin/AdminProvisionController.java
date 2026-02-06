package com.nexashop.api.controller.admin;

import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.application.usecase.AdminProvisionUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/provision")
public class AdminProvisionController {

    private final AdminProvisionUseCase provisionUseCase;

    public AdminProvisionController(AdminProvisionUseCase provisionUseCase) {
        this.provisionUseCase = provisionUseCase;
    }

    @PostMapping("/tenants")
    public void provisionAllTenants() {
        SecurityContextUtil.requireSuperAdmin();
        provisionUseCase.provisionAllTenants();
    }
}
