package ru.dreadblade.czarbank.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.dreadblade.czarbank.exception.BankAccountTypeInUseException;
import ru.dreadblade.czarbank.exception.BankAccountTypeNameAlreadyExistsException;
import ru.dreadblade.czarbank.exception.BankAccountTypeNotFoundException;
import ru.dreadblade.czarbank.exception.model.ErrorResponse;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class BankAccountTypeExceptionHandler {
    @ExceptionHandler(BankAccountTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBankAccountTypeNotFoundException(BankAccountTypeNotFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(BankAccountTypeInUseException.class)
    public ResponseEntity<ErrorResponse> handleBankAccountTypeInUseException(BankAccountTypeInUseException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(BankAccountTypeNameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBankAccountTypeNameAlreadyExistsException(BankAccountTypeNameAlreadyExistsException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}
