package ru.dreadblade.czarbank.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionMessage {
    BANK_ACCOUNT_TYPE_NOT_FOUND("Bank account type doesn't exist", HttpStatus.NOT_FOUND),
    BANK_ACCOUNT_NOT_FOUND("Bank account doesn't exist", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND("Permission doesn't exist", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("Role doesn't exist", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("User doesn't exist", HttpStatus.NOT_FOUND),

    BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS("Bank account type with same name already exists", HttpStatus.BAD_REQUEST),
    ROLE_NAME_ALREADY_EXISTS("Role with same name already exists", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS("User with same username already exists", HttpStatus.BAD_REQUEST),
    USER_EMAIL_ALREADY_EXISTS("Role with same email already exists", HttpStatus.BAD_REQUEST),

    BANK_ACCOUNT_TYPE_IN_USE("Bank account type in use", HttpStatus.BAD_REQUEST),

    NOT_ENOUGH_BALANCE("Not enough balance", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ExceptionMessage(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}