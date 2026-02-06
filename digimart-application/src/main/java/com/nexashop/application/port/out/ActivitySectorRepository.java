package com.nexashop.application.port.out;

import com.nexashop.domain.tenant.entity.ActivitySector;
import java.util.List;
import java.util.Optional;

public interface ActivitySectorRepository extends CrudRepositoryPort<ActivitySector, Long> {

    Optional<ActivitySector> findByLabelIgnoreCase(String label);

    List<ActivitySector> findByActiveTrue();
}
