package ru.dreadblade.czarbank.exception;

public class UnsupportedCurrencyException extends BaseException {
    public UnsupportedCurrencyException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage(), exceptionMessage.getStatus());
    }
}
