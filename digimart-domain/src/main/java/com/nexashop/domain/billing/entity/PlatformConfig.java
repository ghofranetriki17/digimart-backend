package com.nexashop.domain.billing.entity;

import com.nexashop.domain.common.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "platform_config",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"config_key"})
        }
)
@Getter
@Setter
public class PlatformConfig extends AuditableEntity {

    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    private String description;

    @Column(name = "updated_by")
    private Long updatedBy;
}
