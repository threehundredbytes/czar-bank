package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.BankAccountTypeRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.exception.BankAccountTypeInUseException;
import ru.dreadblade.czarbank.exception.BankAccountTypeNameAlreadyExistsException;
import ru.dreadblade.czarbank.exception.BankAccountTypeNotFoundException;
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
            throw new BankAccountTypeNameAlreadyExistsException("Bank account type with name \"" +
                    requestDTO.getName() + "\" already exists");
        }

        return bankAccountTypeRepository.save(BankAccountType.builder()
                .name(requestDTO.getName())
                .transactionCommission(requestDTO.getTransactionCommission())
                .build());
    }

    public BankAccountType updateById(long id, BankAccountTypeRequestDTO requestDTO) {
        BankAccountType bankAccountTypeToUpdate = bankAccountTypeRepository.findById(id)
                .orElseThrow(() -> new BankAccountNotFoundException("Bank account type doesn't exist"));

        if (bankAccountTypeRepository.existsByName(requestDTO.getName())) {
            throw new BankAccountTypeNameAlreadyExistsException("Bank account type with name \"" +
                    requestDTO.getName() + "\" already exists");
        }

        String name = requestDTO.getName();

        if (StringUtils.isNotBlank(name)) {
            bankAccountTypeToUpdate.setName(name);
        }

        BigDecimal transactionCommission = requestDTO.getTransactionCommission();

        if (transactionCommission != null) {
            bankAccountTypeToUpdate.setTransactionCommission(transactionCommission);
        }

        return bankAccountTypeRepository.save(bankAccountTypeToUpdate);
    }

    public void deleteById(long id) {
        if (bankAccountTypeRepository.existsById(id)) {
            if (!bankAccountTypeRepository.isTypeUsedByBankAccount(id)) {
                bankAccountTypeRepository.deleteById(id);
            } else {
                String typeName = bankAccountTypeRepository.findById(id).orElseThrow().getName();
                throw new BankAccountTypeInUseException("Bank account type \"" + typeName + "\" in use");
            }
        } else {
            throw new BankAccountTypeNotFoundException("Bank account type doesn't exist");
        }
    }
}
