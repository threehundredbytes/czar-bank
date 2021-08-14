package ru.dreadblade.czarbank.exception;

public class BankAccountTypeInUseException extends RuntimeException {
    public BankAccountTypeInUseException(String message) {
        super(message);
    }
}
