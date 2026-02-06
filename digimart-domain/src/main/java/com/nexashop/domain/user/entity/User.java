package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends TenantEntity {

    private String email;

    private String passwordHash;

    private String firstName;

    private String lastName;

    private String phone;

    private String imageUrl;

    private boolean enabled = true;

    private LocalDateTime lastLogin;
}
