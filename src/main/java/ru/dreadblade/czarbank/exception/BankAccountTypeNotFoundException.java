package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class BankAccountTypeNotFoundException extends BaseException {
    public BankAccountTypeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
