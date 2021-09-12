package ru.dreadblade.czarbank.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.dreadblade.czarbank.api.model.response.CzarBankErrorResponseDTO;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class AccessTokenExceptionHandler {

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleJsonWebTokenExpiredException(TokenExpiredException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(CzarBankErrorResponseDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Access token expired")
                .path(request.getRequestURI())
                .build());
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<CzarBankErrorResponseDTO> handleJsonWebTokenVerificationException(JWTVerificationException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).body(CzarBankErrorResponseDTO.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Access token is invalid")
                .path(request.getRequestURI())
                .build());
    }
}
