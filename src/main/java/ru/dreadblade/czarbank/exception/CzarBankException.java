package ru.dreadblade.czarbank.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class CzarBankException extends RuntimeException {
    private HttpStatus status;

    public CzarBankException(ExceptionMessage exceptionMessage) {
        super(exceptionMessage.getMessage());
        this.status = exceptionMessage.getStatus();
    }
}
