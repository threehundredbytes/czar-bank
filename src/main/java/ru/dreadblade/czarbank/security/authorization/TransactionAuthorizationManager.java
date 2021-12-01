package ru.dreadblade.czarbank.security.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;

@Component
public class TransactionAuthorizationManager {

    private final BankAccountRepository bankAccountRepository;

    @Autowired
    public TransactionAuthorizationManager(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    public boolean isCurrentUserTheOwnerOfBankAccount(Long bankAccountId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            return false;
        }

        User currentUser = (User) principal;

        BankAccount sourceBankAccount = bankAccountRepository.findById(bankAccountId).orElseThrow(() ->
                new CzarBankException(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND)
        );

        return sourceBankAccount.getOwner().getId().equals(currentUser.getId());
    }

    public boolean isCurrentUserTheOwnerOfSourceBankAccount(String bankAccountNumber) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof User)) {
            return false;
        }

        User currentUser = (User) principal;

        BankAccount sourceBankAccount = bankAccountRepository.findByNumber(bankAccountNumber).orElseThrow(() ->
                new CzarBankException(ExceptionMessage.SOURCE_BANK_ACCOUNT_DOESNT_EXIST)
        );

        return sourceBankAccount.getOwner().getId().equals(currentUser.getId());
    }
}
