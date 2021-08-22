package ru.dreadblade.czarbank.exception;

public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage(), exceptionMessage.getStatus());
    }
}
