package ru.dreadblade.czarbank.exception.util;

import org.springframework.http.ResponseEntity;
import ru.dreadblade.czarbank.exception.BaseException;
import ru.dreadblade.czarbank.exception.model.ErrorResponse;

import javax.servlet.http.HttpServletRequest;

public class ExceptionHandlingUtils {
    public static ResponseEntity<ErrorResponse> createErrorResponse(BaseException exception, HttpServletRequest request) {
        return ResponseEntity.status(exception.getStatus())
                .body(ErrorResponse.builder()
                        .status(exception.getStatus().value())
                        .error(exception.getStatus().getReasonPhrase())
                        .message(exception.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}
