package com.nexashop.infrastructure.persistence.model.user;

import com.nexashop.infrastructure.persistence.model.common.TenantScopedJpaEntity;
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
public class UserRoleAssignmentJpaEntity extends TenantScopedJpaEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(nullable = false)
    private boolean active = true;
}
