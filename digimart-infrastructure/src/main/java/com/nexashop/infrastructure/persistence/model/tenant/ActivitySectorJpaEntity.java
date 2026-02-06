package com.nexashop.infrastructure.persistence.model.tenant;

import com.nexashop.infrastructure.persistence.model.common.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "activity_sectors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"label"})
        }
)
@Getter
@Setter
public class ActivitySectorJpaEntity extends AuditableJpaEntity {

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
