package ru.dreadblade.czarbank.exception;

import javax.persistence.EntityNotFoundException;

public class BankAccountNotFoundException extends EntityNotFoundException {
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}
