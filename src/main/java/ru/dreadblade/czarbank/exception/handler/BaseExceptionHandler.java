package ru.dreadblade.czarbank.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.dreadblade.czarbank.exception.BaseException;
import ru.dreadblade.czarbank.exception.model.ErrorResponse;
import ru.dreadblade.czarbank.exception.util.ExceptionHandlingUtils;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBankAccountNotFoundException(BaseException exception, HttpServletRequest request) {
        return ExceptionHandlingUtils.createErrorResponse(exception, request);
    }
}
