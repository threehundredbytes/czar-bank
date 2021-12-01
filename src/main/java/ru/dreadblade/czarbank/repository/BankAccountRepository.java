package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dreadblade.czarbank.domain.BankAccount;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByNumber(String number);

    @Query("select b from BankAccount as b " +
            "inner join User as u on b.owner.id = u.id " +
            "where u.id = :ownerId")
    List<BankAccount> findAllByOwnerId(Long ownerId);
}
