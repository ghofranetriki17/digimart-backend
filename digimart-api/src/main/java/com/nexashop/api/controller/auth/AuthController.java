package com.nexashop.api.controller.auth;

import com.nexashop.api.dto.request.auth.LoginRequest;
import com.nexashop.api.dto.request.auth.RegisterTenantStep1Request;
import com.nexashop.api.dto.request.auth.RegisterTenantStep2Request;
import com.nexashop.api.dto.response.auth.LoginResponse;
import com.nexashop.api.dto.response.auth.RegisterTenantStep1Response;
import com.nexashop.api.security.AuthTokenService;
import com.nexashop.api.service.TenantProvisioningService;
import com.nexashop.domain.tenant.entity.ActivitySector;
import com.nexashop.domain.tenant.entity.Tenant;
import com.nexashop.domain.user.entity.User;
import com.nexashop.domain.user.entity.Role;
import com.nexashop.domain.user.entity.UserRoleAssignment;
import com.nexashop.infrastructure.persistence.jpa.ActivitySectorJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.RoleJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserRoleAssignmentJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserJpaRepository userRepository;
    private final UserRoleAssignmentJpaRepository assignmentRepository;
    private final RoleJpaRepository roleRepository;
    private final TenantJpaRepository tenantRepository;
    private final ActivitySectorJpaRepository sectorRepository;
    private final AuthTokenService tokenService;
    private final TenantProvisioningService provisioningService;

    public AuthController(
            UserJpaRepository userRepository,
            UserRoleAssignmentJpaRepository assignmentRepository,
            RoleJpaRepository roleRepository,
            TenantJpaRepository tenantRepository,
            ActivitySectorJpaRepository sectorRepository,
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

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.isEnabled() || !request.getPassword().equals(user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
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

        Long sectorId = tenantRepository.findById(user.getTenantId())
                .map(Tenant::getSectorId)
                .orElse(null);
        String sectorLabel = sectorId == null
                ? null
                : sectorRepository.findById(sectorId)
                    .map(ActivitySector::getLabel)
                    .orElse(null);

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .tenantId(user.getTenantId())
                .sectorId(sectorId)
                .sectorLabel(sectorLabel)
                .roles(roles)
                .build();
    }

    @PostMapping("/register-tenant")
    public void registerTenantLegacy() {
        throw new ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Use /api/auth/register-tenant/step1 then /api/auth/register-tenant/step2"
        );
    }

    @PostMapping("/register-tenant/step1")
    public RegisterTenantStep1Response registerTenantStep1(
            @Valid @RequestBody RegisterTenantStep1Request request
    ) {
        String normalizedSubdomain = normalizeSubdomain(request.getTenantName());
        if (tenantRepository.existsBySubdomain(normalizedSubdomain)) {
            throw new ResponseStatusException(CONFLICT, "Subdomain already exists");
        }

        Tenant tenant = new Tenant();
        tenant.setName(request.getTenantName());
        tenant.setSubdomain(normalizedSubdomain);
        tenant.setContactEmail(request.getContactEmail());
        tenant.setContactPhone(request.getContactPhone());
        tenant.setLogoUrl(request.getLogoUrl());
        tenant.setStatus(request.getStatus());
        tenant.setDefaultLocale(request.getDefaultLocale());
        tenant.setSectorId(resolveSectorId(request.getSectorId()));

        Tenant savedTenant = tenantRepository.save(tenant);
        provisioningService.provisionTenant(savedTenant.getId());
        return RegisterTenantStep1Response.builder()
                .tenantId(savedTenant.getId())
                .subdomain(savedTenant.getSubdomain())
                .build();
    }

    @PostMapping("/register-tenant/step2")
    public LoginResponse registerTenantStep2(
            @Valid @RequestBody RegisterTenantStep2Request request,
            HttpServletRequest httpRequest
    ) {
        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Tenant not found"));
        if (userRepository.existsByEmail(request.getOwnerEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        User owner = new User();
        owner.setTenantId(tenant.getId());
        owner.setEmail(request.getOwnerEmail());
        owner.setPasswordHash(request.getOwnerPassword());
        owner.setFirstName(request.getOwnerFirstName());
        owner.setLastName(request.getOwnerLastName());
        User savedOwner = userRepository.save(owner);

        assignRole(savedOwner, "OWNER", "Tenant Owner");

        Set<String> roles = assignmentRepository
                .findByTenantIdAndUserIdAndActiveTrue(savedOwner.getTenantId(), savedOwner.getId())
                .stream()
                .map(UserRoleAssignment::getRoleId)
                .collect(Collectors.toSet())
                .stream()
                .flatMap(roleId -> roleRepository.findById(roleId).stream())
                .map(role -> role.getCode())
                .collect(Collectors.toSet());

        roles = ensureSuperAdminForFirstUser(savedOwner, roles);

        String token = tokenService.createToken(
                savedOwner.getTenantId(),
                savedOwner.getId(),
                httpRequest.getHeader("User-Agent")
        );

        String sectorLabel = tenant.getSectorId() == null
                ? null
                : sectorRepository.findById(tenant.getSectorId())
                    .map(ActivitySector::getLabel)
                    .orElse(null);

        return LoginResponse.builder()
                .token(token)
                .userId(savedOwner.getId())
                .tenantId(savedOwner.getTenantId())
                .sectorId(tenant.getSectorId())
                .sectorLabel(sectorLabel)
                .roles(roles)
                .build();
    }

    private Long resolveSectorId(Long sectorId) {
        if (sectorId == null) {
            return null;
        }
        ActivitySector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResponseStatusException(CONFLICT, "Activity sector not found"));
        if (!sector.isActive()) {
            throw new ResponseStatusException(CONFLICT, "Activity sector is inactive");
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
