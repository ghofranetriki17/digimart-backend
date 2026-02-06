package com.nexashop.application.service;

import com.nexashop.application.port.out.RefreshTokenRepository;
import com.nexashop.domain.user.entity.RefreshToken;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
public class AuthTokenService {

    private static final int TOKEN_DAYS_VALID = 7;

    private final RefreshTokenRepository refreshTokenRepository;

    public AuthTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String createToken(Long tenantId, Long userId, String deviceInfo) {
        String rawToken = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        String hash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTenantId(tenantId);
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(hash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(TOKEN_DAYS_VALID));
        refreshToken.setDeviceInfo(deviceInfo);

        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    public Optional<RefreshToken> validateToken(String rawToken) {
        String hash = hashToken(rawToken);
        return refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                .filter(token -> token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
