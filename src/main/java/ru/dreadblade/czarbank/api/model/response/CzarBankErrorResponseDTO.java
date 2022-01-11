package ru.dreadblade.czarbank.api.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import ru.dreadblade.czarbank.api.model.response.validation.ValidationError;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CzarBankErrorResponseDTO {
    @Builder.Default
    private Instant timestamp = Instant.now();
    private int status;
    private String error;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ValidationError> errors;

    private String message;
    private String path;
}
