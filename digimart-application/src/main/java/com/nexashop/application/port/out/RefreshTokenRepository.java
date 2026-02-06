package com.nexashop.application.port.out;

import com.nexashop.domain.user.entity.RefreshToken;
import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepositoryPort<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}
