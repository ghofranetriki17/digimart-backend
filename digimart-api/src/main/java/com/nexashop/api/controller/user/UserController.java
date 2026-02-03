package com.nexashop.api.controller.user;

import com.nexashop.api.dto.request.user.CreateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRequest;
import com.nexashop.api.dto.request.user.UpdateUserRolesRequest;
import com.nexashop.api.dto.response.user.UserResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
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
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final TenantJpaRepository tenantRepository;
    private final UserJpaRepository userRepository;
    private final RoleJpaRepository roleRepository;
    private final UserRoleAssignmentJpaRepository assignmentRepository;

    public UserController(
            TenantJpaRepository tenantRepository,
            UserJpaRepository userRepository,
            RoleJpaRepository roleRepository,
            UserRoleAssignmentJpaRepository assignmentRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        Long tenantId = request.getTenantId();
        if (tenantId == null) {
            tenantId = requesterTenantId;
        }
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        long existingUsers = userRepository.countByTenantId(tenantId);
        long totalUsers = userRepository.count();

        User user = new User();
        user.setTenantId(tenantId);
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setImageUrl(request.getImageUrl());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        if (totalUsers == 0) {
            assignRole(saved, "SUPER_ADMIN", "Platform Admin");
        }
        if (existingUsers == 0) {
            assignRole(saved, "OWNER", "Tenant Owner");
        }
        return ResponseEntity
                .created(URI.create("/api/users/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping
    public List<UserResponse> listUsers(@RequestParam Long tenantId) {
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return userRepository.findByTenantId(tenantId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (!SecurityContextUtil.requireUser().getTenantId().equals(user.getTenantId())
                && !SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")) {
            throw new ResponseStatusException(FORBIDDEN, "Cross-tenant access forbidden");
        }
        return toResponse(user);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !user.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setImageUrl(request.getImageUrl());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        return toResponse(userRepository.save(user));
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRolesRequest request
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        Long requesterTenantId = SecurityContextUtil.requireUser().getTenantId();
        if (!SecurityContextUtil.requireUser().hasRole("SUPER_ADMIN")
                && !user.getTenantId().equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }

        java.util.Set<String> desired = request.getRoles() == null
                ? java.util.Set.of()
                : request.getRoles().stream()
                        .filter(role -> role != null && !role.isBlank())
                        .collect(java.util.stream.Collectors.toSet());

        java.util.List<Role> desiredRoles = desired.isEmpty()
                ? java.util.List.of()
                : roleRepository.findByTenantIdAndCodeIn(user.getTenantId(), desired);

        if (desiredRoles.size() != desired.size()) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "One or more roles are invalid"
            );
        }

        java.util.List<UserRoleAssignment> existing = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId());

        java.util.Set<Long> desiredRoleIds = desiredRoles.stream()
                .map(Role::getId)
                .collect(java.util.stream.Collectors.toSet());

        for (Role role : desiredRoles) {
            assignmentRepository.findByTenantIdAndUserIdAndRoleId(
                            user.getTenantId(),
                            user.getId(),
                            role.getId()
                    )
                    .orElseGet(() -> {
                        UserRoleAssignment assignment = new UserRoleAssignment();
                        assignment.setTenantId(user.getTenantId());
                        assignment.setUserId(user.getId());
                        assignment.setRoleId(role.getId());
                        assignment.setActive(true);
                        return assignmentRepository.save(assignment);
                    });
        }

        for (UserRoleAssignment assignment : existing) {
            if (!desiredRoleIds.contains(assignment.getRoleId())) {
                assignment.setActive(false);
                assignmentRepository.save(assignment);
            }
        }

        return toResponse(user);
    }

    @PostMapping("/{id}/roles/admin")
    public UserResponse grantAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        SecurityContextUtil.requireOwnerOrAdmin(user.getTenantId());

        assignRole(user, "ADMIN", "Tenant Admin");
        return toResponse(user);
    }

    @PostMapping("/{id}/roles/super-admin")
    public UserResponse grantSuperAdmin(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        SecurityContextUtil.requireSuperAdmin();

        assignRole(user, "SUPER_ADMIN", "Platform Admin");
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        java.util.Set<Long> roleIds = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Set<String> roles = roleIds.isEmpty()
                ? java.util.Set.of()
                : roleRepository.findByTenantIdAndIdIn(user.getTenantId(), roleIds).stream()
                        .map(Role::getCode)
                        .collect(java.util.stream.Collectors.toSet());
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

    private void assignRole(User user, String code, String label) {
        Role role = roleRepository.findByTenantIdAndCode(user.getTenantId(), code)
                .orElseGet(() -> {
                    Role created = new Role();
                    created.setTenantId(user.getTenantId());
                    created.setCode(code);
                    created.setLabel(label);
                    created.setSystemRole(true);
                    return roleRepository.save(created);
                });

        assignmentRepository.findByTenantIdAndUserIdAndRoleId(
                        user.getTenantId(),
                        user.getId(),
                        role.getId()
                )
                .orElseGet(() -> {
                    UserRoleAssignment assignment = new UserRoleAssignment();
                    assignment.setTenantId(user.getTenantId());
                    assignment.setUserId(user.getId());
                    assignment.setRoleId(role.getId());
                    assignment.setActive(true);
                    return assignmentRepository.save(assignment);
                });
    }
}
