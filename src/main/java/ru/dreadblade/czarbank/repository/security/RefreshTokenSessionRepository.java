package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;

import java.util.Optional;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    Optional<RefreshTokenSession> findByRefreshToken(String refreshToken);

    boolean existsByRefreshToken(String refreshToken);
}
