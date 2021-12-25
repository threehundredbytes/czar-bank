package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.security.BlacklistedAccessToken;

import java.time.Instant;
import java.util.List;

public interface BlacklistedAccessTokenRepository extends JpaRepository<BlacklistedAccessToken, Long> {
    boolean existsByAccessToken(String accessToken);

    List<BlacklistedAccessToken> findAllByCreatedAtIsBefore(Instant instant);
}