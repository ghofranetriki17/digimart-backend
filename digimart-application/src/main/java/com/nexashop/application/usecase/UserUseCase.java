package com.nexashop.application.usecase;

import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class UserUseCase {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleAssignmentRepository assignmentRepository;

    public UserUseCase(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleAssignmentRepository assignmentRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public User createUser(User user, Long targetTenantId, Long requesterTenantId, boolean isSuperAdmin) {
        Long tenantId = targetTenantId == null ? requesterTenantId : targetTenantId;
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        if (!tenantRepository.existsById(tenantId)) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
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

    public List<User> listUsers(Long tenantId, Long requesterTenantId, boolean isSuperAdmin) {
        if (!isSuperAdmin && !tenantId.equals(requesterTenantId)) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
        }
        return userRepository.findByTenantId(tenantId);
    }

    public User getUser(Long id, Long requesterTenantId, boolean isSuperAdmin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Cross-tenant access forbidden");
        }
        return user;
    }

    public User updateUser(
            Long id,
            String firstName,
            String lastName,
            String phone,
            String imageUrl,
            Boolean enabled,
            Long requesterTenantId,
            boolean isSuperAdmin
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
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

    public User updateUserRoles(Long id, Set<String> roles, Long requesterTenantId, boolean isSuperAdmin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        if (!isSuperAdmin && !requesterTenantId.equals(user.getTenantId())) {
            throw new ResponseStatusException(FORBIDDEN, "Tenant access required");
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
                throw new ResponseStatusException(BAD_REQUEST, "One or more roles are invalid");
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        assignRole(user, "ADMIN", "Tenant Admin");
        return user;
    }

    public User grantSuperAdmin(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
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
