package ru.dreadblade.czarbank.exception;

public class UniqueFieldAlreadyExistsException extends BaseException {
    public UniqueFieldAlreadyExistsException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage(), exceptionMessage.getStatus());
    }
}
