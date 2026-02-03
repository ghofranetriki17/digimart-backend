package com.nexashop.api.dto.response.user;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private Long tenantId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String imageUrl;
    private boolean enabled;
    private java.util.Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLogin;
}
