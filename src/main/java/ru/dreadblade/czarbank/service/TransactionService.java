package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.exception.NotEnoughBalanceException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findAllByBankAccountId(Long bankAccountId) {
        if (bankAccountRepository.existsById(bankAccountId)) {
            return transactionRepository.findAllByBankAccountId(bankAccountId);
        }

        throw new BankAccountNotFoundException("Bank account doesn't exist");
    }

    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        BigDecimal transactionAmount = transaction.getAmount();

        BankAccount source = bankAccountRepository.findById(transaction.getSourceBankAccount().getId()).orElseThrow(
                () -> new BankAccountNotFoundException("Bank account doesn't exist"));

        if (source.getBalance().compareTo(transactionAmount) < 0) {
            throw new NotEnoughBalanceException("Not enough balance");
        }

        BankAccount destination = bankAccountRepository.findById(transaction.getDestinationBankAccount().getId())
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account doesn't exist"));

        source.setBalance(source.getBalance().subtract(transactionAmount));

        destination.setBalance(destination.getBalance().add(transactionAmount));

        return transactionRepository.save(transaction);
    }
}
