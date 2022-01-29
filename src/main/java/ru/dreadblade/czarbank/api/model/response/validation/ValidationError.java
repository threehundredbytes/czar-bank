package ru.dreadblade.czarbank.api.model.response.validation;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String field;
    private String message;
}
