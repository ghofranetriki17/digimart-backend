package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.tenant.ActivitySectorJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ActivitySectorJpaRepository extends JpaRepository<ActivitySectorJpaEntity, Long> {
    Optional<ActivitySectorJpaEntity> findByLabelIgnoreCase(String label);

    List<ActivitySectorJpaEntity> findByActiveTrue();

    Page<ActivitySectorJpaEntity> findByActiveTrue(Pageable pageable);
}

