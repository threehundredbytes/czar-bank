package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dreadblade.czarbank.domain.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select t from Transaction as t " +
            "inner join BankAccount b on t.sourceBankAccount.id = b.id or " +
            "t.destinationBankAccount.id = b.id " +
            "where b.id = :bankAccountId " +
            "order by t.datetime desc")
    List<Transaction> findAllByBankAccountId(Long bankAccountId);
}
