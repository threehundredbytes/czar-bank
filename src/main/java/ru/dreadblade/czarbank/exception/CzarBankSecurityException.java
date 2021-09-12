package ru.dreadblade.czarbank.exception;

public class CzarBankSecurityException extends CzarBankException {
    public CzarBankSecurityException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
