package com.nexashop.application.usecase;

import com.nexashop.application.exception.*;
import com.nexashop.application.port.out.ActivitySectorRepository;
import com.nexashop.application.port.out.RoleRepository;
import com.nexashop.application.port.out.TenantRepository;
import com.nexashop.application.port.out.UserRepository;
import com.nexashop.application.port.out.UserRoleAssignmentRepository;
import com.nexashop.application.service.AuthTokenService;
import com.nexashop.application.service.TenantProvisioningService;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;


public class AuthUseCase {

    public record LoginResult(
            String token,
            Long userId,
            Long tenantId,
            Long sectorId,
            String sectorLabel,
            Set<String> roles
    ) {}

    public record RegisterTenantStep1Result(Long tenantId, String subdomain) {}

    private final UserRepository userRepository;
    private final UserRoleAssignmentRepository assignmentRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final ActivitySectorRepository sectorRepository;
    private final AuthTokenService tokenService;
    private final TenantProvisioningService provisioningService;

    public AuthUseCase(
            UserRepository userRepository,
            UserRoleAssignmentRepository assignmentRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            ActivitySectorRepository sectorRepository,
            AuthTokenService tokenService,
            TenantProvisioningService provisioningService
    ) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.sectorRepository = sectorRepository;
        this.tokenService = tokenService;
        this.provisioningService = provisioningService;
    }

    public LoginResult login(String email, String password, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isEnabled() || !password.equals(user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        Set<String> roles = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(user.getTenantId(), user.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet())
                .stream()
                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                .map(Role::getCode)
                .collect(Collectors.toSet());

        roles = ensureSuperAdminForFirstUser(user, roles);
        if (roles.isEmpty()) {
            roles = ensureOwnerIfFirstUser(user);
        }

        String token = tokenService.createToken(
                user.getTenantId(),
                user.getId(),
                userAgent
        );

        Long sectorId = tenantRepository.findById(user.getTenantId())
                .map(Tenant::getSectorId)
                .orElse(null);
        String sectorLabel = sectorId == null
                ? null
                : sectorRepository.findById(sectorId)
                    .map(ActivitySector::getLabel)
                    .orElse(null);

        return new LoginResult(token, user.getId(), user.getTenantId(), sectorId, sectorLabel, roles);
    }

    public RegisterTenantStep1Result registerTenantStep1(
            String tenantName,
            String contactEmail,
            String contactPhone,
            String logoUrl,
            com.nexashop.domain.tenant.entity.TenantStatus status,
            com.nexashop.domain.common.Locale defaultLocale,
            Long sectorId
    ) {
        String normalizedSubdomain = normalizeSubdomain(tenantName);
        if (tenantRepository.existsBySubdomain(normalizedSubdomain)) {
            throw new ConflictException("Subdomain already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setSubdomain(normalizedSubdomain);
        tenant.setContactEmail(contactEmail);
        tenant.setContactPhone(contactPhone);
        tenant.setLogoUrl(logoUrl);
        tenant.setStatus(status);
        tenant.setDefaultLocale(defaultLocale);
        tenant.setSectorId(resolveSectorId(sectorId));

        Tenant savedTenant = tenantRepository.save(tenant);
        provisioningService.provisionTenant(savedTenant.getId());
        return new RegisterTenantStep1Result(savedTenant.getId(), savedTenant.getSubdomain());
    }

    public LoginResult registerTenantStep2(
            Long tenantId,
            String ownerEmail,
            String ownerPassword,
            String ownerFirstName,
            String ownerLastName,
            String userAgent
    ) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ConflictException("Tenant not found"));
        if (userRepository.existsByEmail(ownerEmail)) {
            throw new ConflictException("Email already exists");
        }

        User owner = new User();
        owner.setTenantId(tenant.getId());
        owner.setEmail(ownerEmail);
        owner.setPasswordHash(ownerPassword);
        owner.setFirstName(ownerFirstName);
        owner.setLastName(ownerLastName);
        User savedOwner = userRepository.save(owner);

        assignRole(savedOwner, "OWNER", "Tenant Owner");

        Set<String> roles = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(savedOwner.getTenantId(), savedOwner.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet())
                .stream()
                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                .map(Role::getCode)
                .collect(Collectors.toSet());

        roles = ensureSuperAdminForFirstUser(savedOwner, roles);

        String token = tokenService.createToken(
                savedOwner.getTenantId(),
                savedOwner.getId(),
                userAgent
        );

        String sectorLabel = tenant.getSectorId() == null
                ? null
                : sectorRepository.findById(tenant.getSectorId())
                    .map(ActivitySector::getLabel)
                    .orElse(null);

        return new LoginResult(token, savedOwner.getId(), savedOwner.getTenantId(), tenant.getSectorId(), sectorLabel, roles);
    }

    private Long resolveSectorId(Long sectorId) {
        if (sectorId == null) {
            return null;
        }
        ActivitySector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ConflictException("Activity sector not found"));
        if (!sector.isActive()) {
            throw new ConflictException("Activity sector is inactive");
        }
        return sector.getId();
    }

    private Set<String> ensureOwnerIfFirstUser(User user) {
        return userRepository.findFirstByTenantIdOrderByIdAsc(user.getTenantId())
                .filter(first -> first.getId().equals(user.getId()))
                .map(first -> {
                    assignRole(user, "OWNER", "Tenant Owner");
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

    private String normalizeSubdomain(String tenantName) {
        if (tenantName == null) {
            return null;
        }
        String value = tenantName.trim().toLowerCase();
        value = value.replaceAll("[^a-z0-9]+", "-");
        value = value.replaceAll("^-+", "").replaceAll("-+$", "");
        return value;
    }
}


