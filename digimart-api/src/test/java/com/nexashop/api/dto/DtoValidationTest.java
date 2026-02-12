package com.nexashop.api.dto;

import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.request.auth.RegisterTenantStep1Request;
import com.nexashop.api.dto.request.auth.RegisterTenantStep2Request;
import com.nexashop.api.dto.request.permission.CreatePermissionRequest;
import com.nexashop.api.dto.request.product.CreateProductRequest;
import com.nexashop.api.dto.request.role.CloneRoleRequest;
import com.nexashop.api.dto.request.role.UpdateRoleRequest;
import com.nexashop.api.dto.request.user.CreateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRolesRequest;
import com.nexashop.domain.common.Locale;
import com.nexashop.domain.tenant.entity.TenantStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void loginRequestRequiresEmailAndPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerTenantStep1RequiresFields() {
        RegisterTenantStep1Request request = new RegisterTenantStep1Request();
        request.setTenantName("");
        request.setContactEmail("bad");
        request.setContactPhone("");
        Set<ConstraintViolation<RegisterTenantStep1Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void registerTenantStep1ValidWhenRequiredFieldsPresent() {
        RegisterTenantStep1Request request = new RegisterTenantStep1Request();
        request.setTenantName("DigiMart");
        request.setContactEmail("contact@digimart.tn");
        request.setContactPhone("123");
        request.setStatus(TenantStatus.ACTIVE);
        request.setDefaultLocale(Locale.FR);
        Set<ConstraintViolation<RegisterTenantStep1Request>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void registerTenantStep2RequiresOwnerAndTenantId() {
        RegisterTenantStep2Request request = new RegisterTenantStep2Request();
        request.setOwnerEmail("bad-email");
        Set<ConstraintViolation<RegisterTenantStep2Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createUserRequestAllowsNullTenantIdButRequiresFields() {
        CreateUserRequest request = new CreateUserRequest();
        request.setTenantId(null);
        request.setEmail("user@demo.com");
        request.setPassword("pass");
        request.setFirstName("User");
        request.setLastName("Demo");
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void createPermissionRequestRequiresCodeAndDomain() {
        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("");
        request.setDomain("");
        Set<ConstraintViolation<CreatePermissionRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void cloneRoleRequestRequiresTemplateAndCodeLabel() {
        CloneRoleRequest request = new CloneRoleRequest();
        request.setCode("");
        request.setLabel("");
        Set<ConstraintViolation<CloneRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void updateRoleRequestRequiresLabel() {
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setLabel(" ");
        Set<ConstraintViolation<UpdateRoleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void updateUserRolesRequestAllowsEmptyOrNull() {
        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(null);
        Set<ConstraintViolation<UpdateUserRolesRequest>> violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void createProductRequestRequiresNameAndPrices() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("");
        request.setInitialPrice(null);
        request.setShippingPrice(null);
        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void createProductRequestValidWhenRequiredFieldsPresent() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Produit");
        request.setInitialPrice(BigDecimal.valueOf(10));
        request.setShippingPrice(BigDecimal.valueOf(2));
        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}
