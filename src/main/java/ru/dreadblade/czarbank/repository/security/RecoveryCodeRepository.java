package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.User;

import java.util.Optional;

public interface RecoveryCodeRepository extends JpaRepository<RecoveryCode, Long> {
    Optional<RecoveryCode> findByCodeAndUser(String code, User user);
    void deleteAllByUser(User user);
}
