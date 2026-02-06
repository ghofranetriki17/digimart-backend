package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.CurrentUserProvider;
import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.application.security.CurrentUser;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class UserUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    public UserUseCase(
            CurrentUserProvider currentUserProvider,
            TenantRepository tenantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository
    ) {
        this.currentUserProvider = currentUserProvider;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public User createUser(User user, Long targetTenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new NotFoundException("Tenant not found");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        long existingUsers = userRepository.countByTenantId(tenantId);
        long totalUsers = userRepository.count();

        user.setTenantId(tenantId);
        User saved = userRepository.save(user);

        if (totalUsers == 0) {
            assignRole(saved, "SUPER_ADMIN", "Platform Admin");
        }
        if (existingUsers == 0) {
            assignRole(saved, "OWNER", "Tenant Owner");
        }
        return saved;
    }

    public List<User> listUsers(Long tenantId) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ForbiddenException("Tenant access required");
        }
        return userRepository.findByTenantId(tenantId);
    }

    public User getUser(Long id) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ForbiddenException("Cross-tenant access forbidden");
        }
        return user;
    }

    public User updateUser(
            Long id,
            String firstName,
            String lastName,
            String phone,
            String imageUrl,
            Boolean enabled
    ) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ForbiddenException("Tenant access required");
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setImageUrl(imageUrl);
        if (enabled != null) {
            user.setEnabled(enabled);
        }
        return userRepository.save(user);
    }

    public User updateUserRoles(Long id, Set<String> roles) {
        CurrentUser currentUser = currentUserProvider.requireUser();
        Long requesterTenantId = currentUser.tenantId();
        boolean isSuperAdmin = currentUser.hasRole("SUPER_ADMIN");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ForbiddenException("Tenant access required");
        }

        Set<String> desired = roles == null
                ? Set.of()
                : roles.stream()
                        .filter(role -> role != null && !role.isBlank())
                        .collect(Collectors.toSet());

        List<Role> desiredRoles = desired.isEmpty()
                ? List.of()
                : roleRepository.findByTenantIdAndCodeIn(user.getTenantId(), desired);

        if (desiredRoles.size() != desired.size()) {
            if (isSuperAdmin && user.getTenantId() == 1L) {
                Set<String> foundCodes = desiredRoles.stream()
                        .map(Role::getCode)
                        .collect(Collectors.toSet());
                Set<String> missing = new java.util.HashSet<>(desired);
                missing.removeAll(foundCodes);
                for (String code : missing) {
                    Role created = new Role();
                    created.setTenantId(user.getTenantId());
                    created.setCode(code);
                    created.setLabel(code);
                    created.setSystemRole(false);
                    roleRepository.save(created);
                }
                desiredRoles = roleRepository.findByTenantIdAndCodeIn(user.getTenantId(), desired);
            } else {
                throw new BadRequestException("One or more roles are invalid");
            }
        }

        List<UserRoleAssignment> existing = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId());

        Set<Long> desiredRoleIds = desiredRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

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

        return user;
    }

    public User grantAdmin(Long id) {
        User user = getUser(id);
        assignRole(user, "ADMIN", "Tenant Admin");
        return user;
    }

    public User grantSuperAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        assignRole(user, "SUPER_ADMIN", "Platform Admin");
        return user;
    }

    public Set<String> resolveUserRoleCodes(User user) {
        Set<Long> roleIds = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        return roleRepository.findByTenantIdAndIdIn(user.getTenantId(), roleIds).stream()
                .map(Role::getCode)
                .collect(Collectors.toSet());
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


