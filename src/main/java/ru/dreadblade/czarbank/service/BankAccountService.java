package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.BankAccountTypeRepository;
import ru.dreadblade.czarbank.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountTypeRepository bankAccountTypeRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public BankAccountService(BankAccountRepository bankAccountRepository, BankAccountTypeRepository bankAccountTypeRepository, CurrencyRepository currencyRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.bankAccountTypeRepository = bankAccountTypeRepository;
        this.currencyRepository = currencyRepository;
    }

    public List<BankAccount> findAll() {
        return bankAccountRepository.findAll();
    }

    public BankAccount findById(Long id) {
        return bankAccountRepository.findById(id).orElseThrow(() ->
                new CzarBankException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND)
        );
    }

    public BankAccount create(User owner, Long bankAccountTypeId, Long currencyId) {
        BankAccountType bankAccountType = bankAccountTypeRepository.findById(bankAccountTypeId)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_NOT_FOUND));

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.CURRENCY_NOT_FOUND));

        return bankAccountRepository.save(BankAccount.builder()
                .balance(BigDecimal.ZERO)
                .number(RandomStringUtils.randomNumeric(20))
                .bankAccountType(bankAccountType)
                .usedCurrency(currency)
                .owner(owner)
                .build());
    }

    public void deleteById(Long id) {
        if (bankAccountRepository.existsById(id)) {
            bankAccountRepository.deleteById(id);
        } else {
            throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND);
        }
    }
}
