package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.BankAccountTypeRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.exception.*;
import ru.dreadblade.czarbank.repository.BankAccountTypeRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountTypeService {
    private final BankAccountTypeRepository bankAccountTypeRepository;

    @Autowired
    public BankAccountTypeService(BankAccountTypeRepository bankAccountTypeRepository) {
        this.bankAccountTypeRepository = bankAccountTypeRepository;
    }

    public List<BankAccountType> findAll() {
        return bankAccountTypeRepository.findAll();
    }

    public BankAccountType create(BankAccountTypeRequestDTO requestDTO) {
        if (bankAccountTypeRepository.existsByName(requestDTO.getName())) {
            throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS);
        }

        return bankAccountTypeRepository.save(BankAccountType.builder()
                .name(requestDTO.getName())
                .transactionCommission(requestDTO.getTransactionCommission())
                .currencyExchangeCommission(requestDTO.getCurrencyExchangeCommission())
                .build());
    }

    public BankAccountType updateById(long id, BankAccountTypeRequestDTO requestDTO) {
        BankAccountType bankAccountTypeToUpdate = bankAccountTypeRepository.findById(id)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_NOT_FOUND));

        if (bankAccountTypeRepository.existsByName(requestDTO.getName())) {
            throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS);
        }

        String name = requestDTO.getName();

        if (StringUtils.isNotBlank(name)) {
            bankAccountTypeToUpdate.setName(name);
        }

        BigDecimal transactionCommission = requestDTO.getTransactionCommission();

        if (transactionCommission != null) {
            bankAccountTypeToUpdate.setTransactionCommission(transactionCommission);
        }

        BigDecimal currencyExchangeCommission = requestDTO.getCurrencyExchangeCommission();

        if (currencyExchangeCommission != null) {
            bankAccountTypeToUpdate.setCurrencyExchangeCommission(currencyExchangeCommission);
        }

        return bankAccountTypeRepository.save(bankAccountTypeToUpdate);
    }

    public void deleteById(long id) {
        if (bankAccountTypeRepository.existsById(id)) {
            if (!bankAccountTypeRepository.isTypeUsedByBankAccount(id)) {
                bankAccountTypeRepository.deleteById(id);
            } else {
                throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_IN_USE);
            }
        } else {
            throw new CzarBankException(ExceptionMessage.BANK_ACCOUNT_TYPE_NOT_FOUND);
        }
    }
}
