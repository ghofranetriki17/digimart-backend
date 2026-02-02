package com.nexashop.api.controller.user;

import com.nexashop.api.dto.request.user.CreateUserRequest;
import com.nexashop.api.dto.response.user.UserResponse;
import com.nexashop.domain.user.entity.User;
import com.nexashop.infrastructure.persistence.jpa.TenantJpaRepository;
import com.nexashop.infrastructure.persistence.jpa.UserJpaRepository;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final TenantJpaRepository tenantRepository;
    private final UserJpaRepository userRepository;

    public UserController(
            TenantJpaRepository tenantRepository,
            UserJpaRepository userRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        if (!tenantRepository.existsById(request.getTenantId())) {
            throw new ResponseStatusException(NOT_FOUND, "Tenant not found");
        }
        if (userRepository.existsByTenantIdAndEmail(request.getTenantId(), request.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Email already exists for tenant");
        }

        User user = new User();
        user.setTenantId(request.getTenantId());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        return ResponseEntity
                .created(URI.create("/api/users/" + saved.getId()))
                .body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .tenantId(user.getTenantId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}
