package com.nexashop.api.controller;

import com.nexashop.api.controller.permission.PermissionController;
import com.nexashop.api.dto.request.permission.CreatePermissionRequest;
import com.nexashop.api.dto.response.permission.PermissionResponse;
import com.nexashop.application.usecase.PermissionUseCase;
import com.nexashop.domain.user.entity.Permission;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class PermissionControllerTest {

    @Test
    void listPermissionsReturnsResponses() {
        PermissionUseCase useCase = Mockito.mock(PermissionUseCase.class);
        PermissionController controller = new PermissionController(useCase);

        Permission a = new Permission();
        a.setId(1L);
        a.setDomain("sales");
        a.setCode("B");
        Permission b = new Permission();
        b.setId(2L);
        b.setDomain("inventory");
        b.setCode("A");

        when(useCase.listPermissions()).thenReturn(List.of(a, b));

        List<PermissionResponse> responses = controller.listPermissions();
        assertEquals(2, responses.size());
    }

    @Test
    void createPermissionDelegates() {
        PermissionUseCase useCase = Mockito.mock(PermissionUseCase.class);
        PermissionController controller = new PermissionController(useCase);

        CreatePermissionRequest request = new CreatePermissionRequest();
        request.setCode("INV_VIEW");
        request.setDomain("inventory");
        request.setDescription("View inventory");

        Permission permission = new Permission();
        permission.setId(3L);
        permission.setCode("INV_VIEW");
        permission.setDomain("inventory");

        when(useCase.createPermission("INV_VIEW", "inventory", "View inventory"))
                .thenReturn(permission);

        PermissionResponse response = controller.createPermission(request);
        assertEquals("INV_VIEW", response.getCode());
    }
}
