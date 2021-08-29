package ru.dreadblade.czarbank.security.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.exception.RefreshTokenException;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class RefreshTokenService {
    @Value("${czar-bank.security.json-web-token.refresh-token.expiration-seconds}")
    private Long expirationSeconds;

    private final AccessTokenService accessTokenService;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    public RefreshTokenService(AccessTokenService accessTokenService, RefreshTokenSessionRepository refreshTokenSessionRepository) {
        this.accessTokenService = accessTokenService;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
    }

    public String generateRefreshToken(User user) {
        RefreshTokenSession session = RefreshTokenSession.builder()
                .refreshToken(generateRefreshToken())
                .user(user)
                .build();

        refreshTokenSessionRepository.save(session);

        return session.getRefreshToken();
    }

    public String updateAccessToken(String refreshToken) {
        RefreshTokenSession session = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                .filter(Predicate.not(RefreshTokenSession::getIsRevoked))
                .orElseThrow(() -> new RefreshTokenException(ExceptionMessage.INVALID_REFRESH_TOKEN));

        if (session.getCreatedAt().plusSeconds(expirationSeconds).isBefore(Instant.now())) {
            throw new RefreshTokenException(ExceptionMessage.REFRESH_TOKEN_EXPIRED);
        }

        User user = session.getUser();

        return accessTokenService.generateAccessToken(user);
    }

    public String updateRefreshToken(String refreshToken) {
        RefreshTokenSession session = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                .filter(Predicate.not(RefreshTokenSession::getIsRevoked))
                .orElseThrow(() -> new RefreshTokenException(ExceptionMessage.INVALID_REFRESH_TOKEN));

        if (session.getCreatedAt().plusSeconds(expirationSeconds).isBefore(Instant.now())) {
            throw new RefreshTokenException(ExceptionMessage.REFRESH_TOKEN_EXPIRED);
        }

        session.setIsRevoked(true);

        return generateRefreshToken(session.getUser());
    }

    private String generateRefreshToken() {
        String refreshToken = UUID.randomUUID().toString();

        while (refreshTokenSessionRepository.existsByRefreshToken(refreshToken)) {
            refreshToken = UUID.randomUUID().toString();
        }

        return refreshToken;
    }
}
