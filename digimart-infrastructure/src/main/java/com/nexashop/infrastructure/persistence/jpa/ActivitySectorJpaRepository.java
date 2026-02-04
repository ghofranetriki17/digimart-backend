package com.nexashop.infrastructure.persistence.jpa;

import com.nexashop.domain.tenant.entity.ActivitySector;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivitySectorJpaRepository extends JpaRepository<ActivitySector, Long> {
    Optional<ActivitySector> findByLabelIgnoreCase(String label);

    List<ActivitySector> findByActiveTrue();
}
