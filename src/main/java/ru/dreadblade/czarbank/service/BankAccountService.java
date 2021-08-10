package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public BankAccountService(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<BankAccount> getAll() {
        return bankAccountRepository.findAll();
    }

    public BankAccount findById(Long id) {
        return bankAccountRepository.findById(id).orElseThrow(() ->
                new BankAccountNotFoundException("Bank account doesn't exist")
        );
    }

    public BankAccount create(String owner) {
        return bankAccountRepository.save(BankAccount.builder()
                .balance(BigDecimal.ZERO)
                .number(RandomStringUtils.randomNumeric(20))
                .owner(owner)
                .build());
    }

    public void deleteById(Long id) {
        if (bankAccountRepository.existsById(id)) {
            bankAccountRepository.deleteById(id);
        } else {
            throw new BankAccountNotFoundException("Bank account doesn't exist");
        }
    }
}
