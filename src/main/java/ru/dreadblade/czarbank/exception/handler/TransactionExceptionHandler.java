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
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value())
                .body(ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}
