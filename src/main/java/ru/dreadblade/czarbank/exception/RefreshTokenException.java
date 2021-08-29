package ru.dreadblade.czarbank.exception;

public class RefreshTokenException extends BaseAuthenticationException {
    public RefreshTokenException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage);
    }
}
