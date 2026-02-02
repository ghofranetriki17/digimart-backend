package com.nexashop.domain.user.entity;

import com.nexashop.domain.common.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "user_role_assignments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tenant_id", "user_id", "role_id"})
        }
)
@Getter
@Setter
public class UserRoleAssignment extends TenantEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private boolean active = true;
}
