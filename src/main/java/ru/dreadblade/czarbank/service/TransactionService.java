package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CurrencyService currencyService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, BankAccountRepository bankAccountRepository, CurrencyService currencyService) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.currencyService = currencyService;
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll();
    }

    public List<Transaction> findAllByBankAccountId(Long bankAccountId) {
        if (bankAccountRepository.existsById(bankAccountId)) {
            return transactionRepository.findAllByBankAccountId(bankAccountId);
        }

        throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND);
    }

    @Transactional
    public Transaction createTransaction(TransactionRequestDTO transactionRequest) {
        BankAccount source = bankAccountRepository.findByNumber(transactionRequest.getSourceBankAccountNumber())
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.SOURCE_BANK_ACCOUNT_DOESNT_EXIST));

        BankAccount destination = bankAccountRepository.findByNumber(transactionRequest.getDestinationBankAccountNumber())
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.DESTINATION_BANK_ACCOUNT_DOESNT_EXIST));

        BigDecimal transactionAmount = transactionRequest.getAmount();
        BigDecimal transactionCommissionAmount = transactionAmount.multiply(source.getBankAccountType()
                .getTransactionCommission());

        BigDecimal transactionAmountWithCommission = transactionAmount.add(transactionCommissionAmount);

        if (!source.getUsedCurrency().equals(destination.getUsedCurrency())) {
            transactionCommissionAmount = transactionAmount.multiply(source.getBankAccountType().getCurrencyExchangeCommission());

            transactionAmountWithCommission = transactionAmountWithCommission.add(transactionCommissionAmount);
        }

        if (source.getBalance().compareTo(transactionAmountWithCommission) < 0) {
            throw new CzarBankException(ExceptionMessage.NOT_ENOUGH_BALANCE);
        }

        Transaction transaction = Transaction.builder()
                .amount(transactionAmount)
                .receivedAmount(transactionAmount)
                .sourceBankAccount(source)
                .destinationBankAccount(destination)
                .build();

        source.setBalance(source.getBalance().subtract(transactionAmountWithCommission));

        if (!source.getUsedCurrency().equals(destination.getUsedCurrency())) {
            transaction.setReceivedAmount(currencyService.exchangeCurrency(source.getUsedCurrency(), transactionAmount,
                    destination.getUsedCurrency()));
        }

        destination.setBalance(destination.getBalance().add(transaction.getReceivedAmount()));

        return transactionRepository.save(transaction);
    }
}
