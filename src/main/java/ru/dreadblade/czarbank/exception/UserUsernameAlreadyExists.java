package ru.dreadblade.czarbank.exception;

import org.springframework.http.HttpStatus;

public class UserUsernameAlreadyExists extends BaseException {
    public UserUsernameAlreadyExists(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
