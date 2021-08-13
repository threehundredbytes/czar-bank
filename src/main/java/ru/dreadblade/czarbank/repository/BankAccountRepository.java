package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.BankAccount;

import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByNumber(String number);
}
