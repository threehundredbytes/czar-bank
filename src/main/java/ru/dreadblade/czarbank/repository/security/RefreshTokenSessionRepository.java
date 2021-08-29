package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;

import java.util.Optional;

public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    Optional<RefreshTokenSession> findByRefreshToken(String refreshToken);

    @Query("select count(r) from RefreshTokenSession as r where r.isRevoked = false and r.user.id = :#{#user.id}")
    Long countByUser(@Param("user") User user);

    boolean existsByRefreshToken(String refreshToken);

    @Modifying
    @Query("update RefreshTokenSession as r set r.isRevoked = true where r.user.id = :#{#user.id}")
    void markRevokedAllByUser(@Param("user") User user);
}
