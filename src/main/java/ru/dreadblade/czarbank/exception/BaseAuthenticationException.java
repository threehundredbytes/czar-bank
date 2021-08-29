package ru.dreadblade.czarbank.exception;

public abstract class BaseAuthenticationException extends BaseException {
    public BaseAuthenticationException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage(), exceptionMessage.getStatus());
    }
}
