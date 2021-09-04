package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.exception.EntityNotFoundException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
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

        throw new EntityNotFoundException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND);
    }

    @Transactional
    public Transaction createTransaction(TransactionRequestDTO transactionRequest) {
        BankAccount source = bankAccountRepository.findByNumber(transactionRequest.getSourceBankAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND));

        BigDecimal transactionAmount = transactionRequest.getAmount();

        if (source.getBalance().compareTo(transactionAmount) < 0) {
            throw new NotEnoughBalanceException();
        }

        BankAccount destination = bankAccountRepository.findByNumber(transactionRequest.getDestinationBankAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND));

        if (!source.getUsedCurrency().equals(destination.getUsedCurrency())) {
            throw new NotImplementedException("Currency exchange has not yet been implemented");
        }

        Transaction transaction = Transaction.builder()
                .amount(transactionAmount)
                .sourceBankAccount(source)
                .destinationBankAccount(destination)
                .build();

        BigDecimal transactionCommissionAmount = transactionAmount.multiply(source.getBankAccountType()
                .getTransactionCommission());

        BigDecimal transactionAmountWithCommission = transactionAmount.add(transactionCommissionAmount);

        source.setBalance(source.getBalance().subtract(transactionAmountWithCommission));

        destination.setBalance(destination.getBalance().add(transactionAmount));

        return transactionRepository.save(transaction);
    }
}
