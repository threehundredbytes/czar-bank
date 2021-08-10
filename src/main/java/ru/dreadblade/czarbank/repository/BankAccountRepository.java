package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
}
