package ru.dreadblade.czarbank.exception.handler;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.dreadblade.czarbank.api.model.response.CzarBankErrorResponseDTO;
import ru.dreadblade.czarbank.api.model.response.validation.ValidationError;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class CzarBankExceptionHandler {

    @ExceptionHandler(CzarBankException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleCzarBankException(CzarBankException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(CzarBankErrorResponseDTO.builder()
                        .status(exception.getStatus().value())
                        .error(exception.getStatus().getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }

    @ExceptionHandler(CzarBankSecurityException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleCzarBankSecurityException(CzarBankSecurityException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus()).body(CzarBankErrorResponseDTO.builder()
                .status(exception.getStatus().value())
                .error(exception.getStatus().getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleBindException(BindException exception, HttpServletRequest request) {
        List<ValidationError> errors = exception.getAllErrors().stream().map(e -> {
            if (e instanceof FieldError) {
                return ValidationError.builder()
                        .field(((FieldError) e).getField())
                        .message(e.getDefaultMessage())
                        .build();
            } else {
                return ValidationError.builder()
                        .field(e.getObjectName())
                        .message(e.getDefaultMessage())
                        .build();
            }
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(CzarBankErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Invalid request")
                .error("Validation error")
                .errors(errors)
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleNotImplementedException(NotImplementedException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(CzarBankErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(exception.getMessage())
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleGenericException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(CzarBankErrorResponseDTO.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unknown error occurred. If the problem persists, please, contact support!")
                .path(request.getRequestURI())
                .build());
    }
}
