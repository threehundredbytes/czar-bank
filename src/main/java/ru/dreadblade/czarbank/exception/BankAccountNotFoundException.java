package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class BankAccountNotFoundException extends BaseException {
    public BankAccountNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
