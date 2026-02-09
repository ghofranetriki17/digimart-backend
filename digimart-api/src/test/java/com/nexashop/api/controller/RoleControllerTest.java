package com.nexashop.api.controller;

import com.nexashop.api.controller.role.RoleController;
import com.nexashop.api.dto.request.role.CloneRoleRequest;
import com.nexashop.api.dto.request.role.CreateRoleRequest;
import com.nexashop.api.dto.request.role.CreateRoleTemplateRequest;
import com.nexashop.api.dto.request.role.UpdateRolePermissionsRequest;
import com.nexashop.api.dto.request.role.UpdateRoleRequest;
import com.nexashop.api.dto.response.role.RoleResponse;
import com.nexashop.application.usecase.RoleUseCase;
import com.nexashop.domain.user.entity.Role;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class RoleControllerTest {

    @Test
    void listRolesReturnsResponses() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        Role role = new Role();
        role.setId(1L);
        role.setTenantId(2L);
        role.setCode("ADMIN");
        role.setLabel("Admin");
        role.setSystemRole(false);

        when(roleUseCase.listRoles(2L))
                .thenReturn(List.of(new RoleUseCase.RoleDetails(role, Set.of("STORE_VIEW"))));

        List<RoleResponse> responses = controller.listRoles(2L);
        assertEquals(1, responses.size());
        assertEquals("ADMIN", responses.get(0).getCode());
        assertEquals(Set.of("STORE_VIEW"), responses.get(0).getPermissions());
    }

    @Test
    void createRoleDelegates() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        CreateRoleRequest request = new CreateRoleRequest();
        request.setCode("MANAGER");
        request.setLabel("Manager");
        request.setTargetTenantId(5L);

        Role role = new Role();
        role.setId(7L);
        role.setTenantId(5L);
        role.setCode("MANAGER");
        role.setLabel("Manager");
        role.setSystemRole(false);

        when(roleUseCase.createRole("MANAGER", "Manager", 5L))
                .thenReturn(new RoleUseCase.RoleDetails(role, Set.of("USER_VIEW")));

        RoleResponse response = controller.createRole(request);
        assertEquals("MANAGER", response.getCode());
        assertEquals(5L, response.getTenantId());
    }

    @Test
    void createTemplateDelegates() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        CreateRoleTemplateRequest request = new CreateRoleTemplateRequest();
        request.setCode("TEMPLATE_SUPPORT");
        request.setLabel("Support Template");

        Role role = new Role();
        role.setId(9L);
        role.setTenantId(0L);
        role.setCode("TEMPLATE_SUPPORT");
        role.setLabel("Support Template");
        role.setSystemRole(true);

        when(roleUseCase.createTemplate("TEMPLATE_SUPPORT", "Support Template"))
                .thenReturn(new RoleUseCase.RoleDetails(role, Set.of()));

        RoleResponse response = controller.createTemplate(request);
        assertEquals("TEMPLATE_SUPPORT", response.getCode());
        assertEquals(0L, response.getTenantId());
    }

    @Test
    void updateRolePermissionsReturnsEmptyWhenNoPerms() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        UpdateRolePermissionsRequest request = new UpdateRolePermissionsRequest();
        request.setPermissionCodes(null);

        Role role = new Role();
        role.setId(3L);
        role.setTenantId(1L);
        role.setCode("ADMIN");

        when(roleUseCase.updateRolePermissions(3L, null))
                .thenReturn(new RoleUseCase.RoleDetails(role, Set.of()));

        RoleResponse response = controller.updateRolePermissions(3L, request);
        assertEquals(0, response.getPermissions().size());
    }

    @Test
    void cloneRoleDelegates() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        CloneRoleRequest request = new CloneRoleRequest();
        request.setTemplateRoleId(1L);
        request.setCode("NEW_ROLE");
        request.setLabel("New Role");
        request.setTargetTenantId(2L);

        Role role = new Role();
        role.setId(11L);
        role.setTenantId(2L);
        role.setCode("NEW_ROLE");
        role.setLabel("New Role");

        when(roleUseCase.cloneRole(1L, "NEW_ROLE", "New Role", 2L))
                .thenReturn(new RoleUseCase.RoleDetails(role, Set.of("ORDER_VIEW")));

        RoleResponse response = controller.cloneRole(request);
        assertEquals("NEW_ROLE", response.getCode());
    }

    @Test
    void updateRoleDelegates() {
        RoleUseCase roleUseCase = Mockito.mock(RoleUseCase.class);
        RoleController controller = new RoleController(roleUseCase);

        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setLabel("New Label");

        Role role = new Role();
        role.setId(4L);
        role.setTenantId(1L);
        role.setCode("ADMIN");
        role.setLabel("New Label");

        when(roleUseCase.updateRole(4L, "New Label"))
                .thenReturn(new RoleUseCase.RoleDetails(role, Set.of()));

        RoleResponse response = controller.updateRole(4L, request);
        assertEquals("New Label", response.getLabel());
    }
}
