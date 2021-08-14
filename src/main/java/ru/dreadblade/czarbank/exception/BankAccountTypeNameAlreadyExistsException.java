package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class BankAccountTypeNameAlreadyExistsException extends BaseException {
    public BankAccountTypeNameAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
