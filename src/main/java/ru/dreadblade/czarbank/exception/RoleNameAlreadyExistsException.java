package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class RoleNameAlreadyExistsException extends BaseException {
    public RoleNameAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
