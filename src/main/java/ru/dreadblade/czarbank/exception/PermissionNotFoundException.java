package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class PermissionNotFoundException extends BaseException {
    public PermissionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
