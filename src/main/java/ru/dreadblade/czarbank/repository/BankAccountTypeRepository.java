package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.BankAccountType;

import java.util.Optional;

public interface BankAccountTypeRepository extends JpaRepository<BankAccountType, Long> {
    Optional<BankAccountType> findByName(String name);
}
