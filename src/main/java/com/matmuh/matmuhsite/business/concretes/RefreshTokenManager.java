package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.RefreshTokenService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.exceptions.InvalidCredentialsException;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.dataAccess.abstracts.RefreshTokenDao;
import com.matmuh.matmuhsite.entities.RefreshToken;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class RefreshTokenManager implements RefreshTokenService {

    private final Logger logger = LoggerFactory.getLogger(RefreshTokenManager.class);

    private final RefreshTokenDao refreshTokenDao;
    private final JwtService jwtService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.refresh-validity-days:30}")
    private long refreshValidityDays;

    private final TransactionTemplate newTransaction;

    public RefreshTokenManager(RefreshTokenDao refreshTokenDao, JwtService jwtService,
                               PlatformTransactionManager transactionManager) {
        this.refreshTokenDao = refreshTokenDao;
        this.jwtService = jwtService;
        this.newTransaction = new TransactionTemplate(transactionManager);
        this.newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    @Transactional
    public AuthLoginResponseDto issueTokens(User user) {
        return issue(user, UUID.randomUUID());
    }

    @Override
    @Transactional
    public AuthLoginResponseDto rotate(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidCredentialsException(AuthMessages.REFRESH_TOKEN_INVALID);
        }

        var stored = refreshTokenDao.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new InvalidCredentialsException(AuthMessages.REFRESH_TOKEN_INVALID));

        var now = Instant.now();

        if (stored.getRevokedAt() != null) {
            logger.warn("Refresh token reuse detected for user {}, revoking family {}",
                    stored.getUser().getId(), stored.getFamilyId());
            newTransaction.executeWithoutResult(status -> refreshTokenDao.revokeFamily(stored.getFamilyId(), now));
            throw new InvalidCredentialsException(AuthMessages.REFRESH_TOKEN_REUSED);
        }

        if (!stored.isActive(now)) {
            throw new InvalidCredentialsException(AuthMessages.REFRESH_TOKEN_EXPIRED);
        }

        stored.setRevokedAt(now);
        refreshTokenDao.save(stored);

        return issue(stored.getUser(), stored.getFamilyId());
    }

    @Override
    @Transactional
    public void revoke(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenDao.findByTokenHash(hash(refreshToken)).ifPresent(stored ->
                refreshTokenDao.revokeFamily(stored.getFamilyId(), Instant.now()));
    }

    private AuthLoginResponseDto issue(User user, UUID familyId) {
        var rawToken = generateRawToken();
        var now = Instant.now();

        var entity = RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .user(user)
                .familyId(familyId)
                .expiresAt(now.plus(Duration.ofDays(refreshValidityDays)))
                .createdAt(now)
                .build();
        refreshTokenDao.save(entity);

        var accessToken = jwtService.generateToken(user);
        return new AuthLoginResponseDto(accessToken, rawToken, jwtService.getTokenValiditySeconds());
    }

    private String generateRawToken() {
        var bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
