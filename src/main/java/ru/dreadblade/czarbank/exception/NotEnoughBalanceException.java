package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class NotEnoughBalanceException extends BaseException {
    public NotEnoughBalanceException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
