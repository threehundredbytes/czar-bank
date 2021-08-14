package ru.dreadblade.czarbank.exception;

public class BankAccountTypeNotFoundException extends RuntimeException {
    public BankAccountTypeNotFoundException(String message) {
        super(message);
    }
}
