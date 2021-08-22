package ru.dreadblade.czarbank.exception;

public class EntityInUseException extends BaseException {
    public EntityInUseException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage(), exceptionMessage.getStatus());
    }
}
