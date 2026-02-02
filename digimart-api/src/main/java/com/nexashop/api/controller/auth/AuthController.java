package com.nexashop.api.controller.auth;

import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.response.auth.LoginResponse;
import com.nexashop.api.security.AuthTokenService;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserJpaRepository userRepository;
    private final UserRoleAssignmentJpaRepository assignmentRepository;
    private final RoleJpaRepository roleRepository;
    private final AuthTokenService tokenService;

    public AuthController(
            UserJpaRepository userRepository,
            UserRoleAssignmentJpaRepository assignmentRepository,
            RoleJpaRepository roleRepository,
            AuthTokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.roleRepository = roleRepository;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userRepository.findByTenantIdAndEmail(request.getTenantId(), request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled() || !request.getPassword().equals(user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        Set<String> roles = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet())
                .stream()
                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        roles = ensureSuperAdminForFirstUser(user, roles);
        if (roles.isEmpty()) {
            roles = ensureOwnerIfFirstUser(user);
        }

        String token = tokenService.createToken(
                user.getTenantId(),
                user.getId(),
                httpRequest.getHeader("User-Agent")
        );

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .roles(roles)
                .build();
    }

    private Set<String> ensureOwnerIfFirstUser(User user) {
        return userRepository.findFirstByTenantIdOrderByIdAsc(user.getTenantId())
                .filter(first -> first.getId().equals(user.getId()))
                .map(first -> {
                    Role role = roleRepository.findByTenantIdAndCode(user.getTenantId(), "OWNER")
                            .orElseGet(() -> {
                                Role created = new Role();
                                created.setTenantId(user.getTenantId());
                                created.setCode("OWNER");
                                created.setLabel("Tenant Owner");
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
                    return Set.of("OWNER");
                })
                .orElse(Set.of());
    }

    private Set<String> ensureSuperAdminForFirstUser(User user, Set<String> roles) {
        boolean isFirst = userRepository.findFirstByOrderByIdAsc()
                .map(first -> first.getId().equals(user.getId()))
                .orElse(false);
        if (!isFirst || roles.contains("SUPER_ADMIN")) {
            return roles;
        }

        Role role = roleRepository.findByTenantIdAndCode(user.getTenantId(), "SUPER_ADMIN")
                .orElseGet(() -> {
                    Role created = new Role();
                    created.setTenantId(user.getTenantId());
                    created.setCode("SUPER_ADMIN");
                    created.setLabel("Platform Admin");
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

        java.util.Set<String> updated = new java.util.HashSet<>(roles);
        updated.add("SUPER_ADMIN");
        return updated;
    }
}
