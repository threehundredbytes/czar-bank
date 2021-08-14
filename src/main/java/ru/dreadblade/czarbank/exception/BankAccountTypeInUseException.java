package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class BankAccountTypeInUseException extends BaseException {
    public BankAccountTypeInUseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
