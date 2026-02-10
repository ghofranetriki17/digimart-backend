package com.nexashop.api.controller;

import com.nexashop.api.controller.user.UserController;
import com.nexashop.api.dto.request.user.CreateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRolesRequest;
import com.nexashop.api.dto.response.user.UserResponse;
import com.nexashop.application.usecase.UserUseCase;
import com.nexashop.domain.user.entity.User;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void createUserReturnsResponse() {
        UserUseCase userUseCase = Mockito.mock(UserUseCase.class);
        UserController controller = new UserController(userUseCase, "");

        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setTenantId(2L);

        User saved = new User();
        saved.setId(10L);
        saved.setTenantId(2L);
        saved.setEmail("test@example.com");

        when(userUseCase.createUser(Mockito.any(User.class), Mockito.eq(2L))).thenReturn(saved);
        when(userUseCase.resolveUserRoleCodes(saved)).thenReturn(Set.of("USER"));

        UserResponse response = controller.createUser(request).getBody();
        assertEquals(10L, response.getId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Set.of("USER"), response.getRoles());
    }

    @Test
    void listUsersReturnsResponses() {
        UserUseCase userUseCase = Mockito.mock(UserUseCase.class);
        UserController controller = new UserController(userUseCase, "");

        User user = new User();
        user.setId(1L);
        user.setTenantId(2L);
        user.setEmail("a@b.com");

        when(userUseCase.listUsers(2L)).thenReturn(List.of(user));
        when(userUseCase.resolveUserRoleCodes(user)).thenReturn(Set.of("OWNER"));

        List<UserResponse> responses = controller.listUsers(2L);
        assertEquals(1, responses.size());
        assertEquals("a@b.com", responses.get(0).getEmail());
    }

    @Test
    void getUserReturnsResponse() {
        UserUseCase userUseCase = Mockito.mock(UserUseCase.class);
        UserController controller = new UserController(userUseCase, "");

        User user = new User();
        user.setId(7L);
        user.setTenantId(3L);
        user.setEmail("z@x.com");

        when(userUseCase.getUser(7L)).thenReturn(user);
        when(userUseCase.resolveUserRoleCodes(user)).thenReturn(Set.of("ADMIN"));

        UserResponse response = controller.getUser(7L);
        assertEquals(7L, response.getId());
        assertEquals(Set.of("ADMIN"), response.getRoles());
    }

    @Test
    void updateUserReturnsResponse() {
        UserUseCase userUseCase = Mockito.mock(UserUseCase.class);
        UserController controller = new UserController(userUseCase, "");

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("New");
        request.setLastName("Name");

        User updated = new User();
        updated.setId(5L);
        updated.setTenantId(1L);
        updated.setFirstName("New");

        when(userUseCase.updateUser(5L, "New", "Name", null, null, null)).thenReturn(updated);
        when(userUseCase.resolveUserRoleCodes(updated)).thenReturn(Set.of("USER"));

        UserResponse response = controller.updateUser(5L, request);
        assertEquals("New", response.getFirstName());
    }

    @Test
    void updateUserRolesReturnsResponse() {
        UserUseCase userUseCase = Mockito.mock(UserUseCase.class);
        UserController controller = new UserController(userUseCase, "");

        UpdateUserRolesRequest request = new UpdateUserRolesRequest();
        request.setRoles(Set.of("ADMIN"));

        User updated = new User();
        updated.setId(9L);
        updated.setTenantId(1L);

        when(userUseCase.updateUserRoles(9L, Set.of("ADMIN"))).thenReturn(updated);
        when(userUseCase.resolveUserRoleCodes(updated)).thenReturn(Set.of("ADMIN"));

        UserResponse response = controller.updateUserRoles(9L, request);
        assertEquals(Set.of("ADMIN"), response.getRoles());
    }
}
