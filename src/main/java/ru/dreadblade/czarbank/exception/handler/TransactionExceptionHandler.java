package ru.dreadblade.czarbank.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.dreadblade.czarbank.exception.NotEnoughBalanceException;
import ru.dreadblade.czarbank.exception.model.ErrorResponse;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class TransactionExceptionHandler {
    @ExceptionHandler(NotEnoughBalanceException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughBalanceException(NotEnoughBalanceException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}
