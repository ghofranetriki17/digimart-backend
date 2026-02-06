package com.nexashop.infrastructure.persistence.model.billing;

import com.nexashop.infrastructure.persistence.model.common.AuditableJpaEntity;
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
public class PlatformConfigJpaEntity extends AuditableJpaEntity {

    @Column(name = "config_key", nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    private String description;

    @Column(name = "updated_by")
    private Long updatedBy;
}
