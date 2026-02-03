package com.nexashop.api.security;

import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;
    private final UserJpaRepository userRepository;
    private final UserRoleAssignmentJpaRepository assignmentRepository;
    private final RoleJpaRepository roleRepository;

    public AuthTokenFilter(
            AuthTokenService authTokenService,
            UserJpaRepository userRepository,
            UserRoleAssignmentJpaRepository assignmentRepository,
            RoleJpaRepository roleRepository
    ) {
        this.authTokenService = authTokenService;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String rawToken = authHeader.substring("Bearer ".length()).trim();
            authTokenService.validateToken(rawToken).ifPresent(token -> {
                userRepository.findById(token.getUserId()).ifPresent(user -> {
                    AuthenticatedUser principal = buildPrincipal(user);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    principal.getRoles().stream()
                                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                            .collect(Collectors.toSet())
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
            });
        }

        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser buildPrincipal(User user) {
        List<UserRoleAssignment> assignments =
                assignmentRepository.findByTenantIdAndUserIdAndActiveTrue(
                        user.getTenantId(),
                        user.getId()
                );
        Set<Long> roleIds = assignments.stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet());
        Set<String> roles = roleIds.isEmpty()
                ? new java.util.HashSet<>()
                : roleRepository.findByTenantIdAndIdIn(user.getTenantId(), roleIds).stream()
                        .map(role -> role.getCode())
                        .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            userRepository.findFirstByTenantIdOrderByIdAsc(user.getTenantId())
                    .filter(first -> first.getId().equals(user.getId()))
                    .ifPresent(firstUser -> {
                        Role ownerRole = roleRepository.findByTenantIdAndCode(
                                        user.getTenantId(),
                                        "OWNER"
                                )
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
                                        ownerRole.getId()
                                )
                                .orElseGet(() -> {
                                    UserRoleAssignment assignment = new UserRoleAssignment();
                                    assignment.setTenantId(user.getTenantId());
                                    assignment.setUserId(user.getId());
                                    assignment.setRoleId(ownerRole.getId());
                                    assignment.setActive(true);
                                    return assignmentRepository.save(assignment);
                                });

                        roles.add("OWNER");
                    });
        }
        return new AuthenticatedUser(user.getId(), user.getTenantId(), roles);
    }
}
