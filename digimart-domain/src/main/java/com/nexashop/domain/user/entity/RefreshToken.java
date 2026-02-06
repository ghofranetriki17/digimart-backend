package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshToken extends TenantEntity {

    private Long userId;

    private String tokenHash;

    private LocalDateTime expiresAt;

    private LocalDateTime revokedAt;

    private String deviceInfo;
}
