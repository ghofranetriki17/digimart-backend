package com.nexashop.api.controller.permission;

import com.nexashop.api.dto.request.permission.CreatePermissionRequest;
import com.nexashop.api.dto.response.permission.PermissionResponse;
import com.nexashop.api.security.SecurityContextUtil;
import com.nexashop.domain.user.entity.Permission;
import com.nexashop.application.port.out.PermissionRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    public PermissionController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public List<PermissionResponse> listPermissions() {
        SecurityContextUtil.requireUser();
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getDomain).thenComparing(Permission::getCode))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @PostMapping
    public PermissionResponse createPermission(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        SecurityContextUtil.requireSuperAdmin();
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new ResponseStatusException(CONFLICT, "Permission code already exists");
        }

        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setDomain(request.getDomain());
        permission.setDescription(request.getDescription());
        return toResponse(permissionRepository.save(permission));
    }

    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .domain(permission.getDomain())
                .description(permission.getDescription())
                .build();
    }
}


