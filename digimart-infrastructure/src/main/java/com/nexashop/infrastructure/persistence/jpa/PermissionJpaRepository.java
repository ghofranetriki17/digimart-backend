package com.nexashop.infrastructure.persistence.jpa;
import com.nexashop.infrastructure.persistence.model.user.PermissionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long> {

    Optional<PermissionJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<PermissionJpaEntity> findByCodeIn(Set<String> codes);
}

