package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dreadblade.czarbank.domain.BankAccountType;

import java.util.Optional;

public interface BankAccountTypeRepository extends JpaRepository<BankAccountType, Long> {
    Optional<BankAccountType> findByName(String name);

    boolean existsByName(String name);

    @Query("select case when count(t) > 0 then true " +
            "else false end " +
            "from BankAccountType as t " +
            "inner join BankAccount as b on b.bankAccountType.id = t.id " +
            "where t.id = :bankAccountTypeId")
    boolean isTypeUsedByBankAccount(long bankAccountTypeId);
}
