package com.nexashop.domain.tenant.entity;

import com.nexashop.domain.common.AuditableEntity;
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
public class ActivitySector extends AuditableEntity {

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
