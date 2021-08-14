package ru.dreadblade.czarbank.exception;

public class BankAccountTypeNameAlreadyExistsException extends RuntimeException {
    public BankAccountTypeNameAlreadyExistsException(String message) {
        super(message);
    }
}
