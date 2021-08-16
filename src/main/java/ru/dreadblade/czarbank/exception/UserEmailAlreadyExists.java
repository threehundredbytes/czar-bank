package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class UserEmailAlreadyExists extends BaseException {
    public UserEmailAlreadyExists(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
