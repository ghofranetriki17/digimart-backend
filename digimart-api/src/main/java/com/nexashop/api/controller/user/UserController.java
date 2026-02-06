package com.nexashop.api.controller.user;

import com.nexashop.api.dto.request.user.CreateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRolesRequest;
import com.nexashop.api.dto.response.user.UserResponse;
import com.nexashop.application.usecase.UserUseCase;
import com.nexashop.domain.user.entity.User;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(
            UserUseCase userUseCase
    ) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setImageUrl(request.getImageUrl());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userUseCase.createUser(
                user,
                request.getTenantId()
        );
        return ResponseEntity
                .created(URI.create("/api/users/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping
    public List<UserResponse> listUsers(@RequestParam Long tenantId) {
        return userUseCase.listUsers(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userUseCase.getUser(id);
        return toResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User saved = userUseCase.updateUser(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getImageUrl(),
                request.getEnabled()
        );
        return toResponse(saved);
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        User updated = userUseCase.updateUserRoles(
                id,
                request.getRoles()
        );
        return toResponse(updated);
    }

    @PostMapping("/{id}/roles/admin")
    public UserResponse grantAdmin(@PathVariable Long id) {
        User user = userUseCase.grantAdmin(id);
        return toResponse(user);
    }

    @PostMapping("/{id}/roles/super-admin")
    public UserResponse grantSuperAdmin(@PathVariable Long id) {
        User user = userUseCase.grantSuperAdmin(id);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        java.util.Set<String> roles = userUseCase.resolveUserRoleCodes(user);
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .imageUrl(user.getImageUrl())
                .enabled(user.isEnabled())
                .roles(roles)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}


