package ru.dreadblade.czarbank.api.model.request;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRequestDTO {
    @NotBlank(message = "Currency code must be not empty", groups = CreateRequest.class)
    @Size(message = "The length of the currency code must be 3 characters", groups = CreateRequest.class, min = 3, max = 3)
    private String code;

    @NotBlank(message = "Currency symbol must be not empty", groups = CreateRequest.class)
    @Size(message = "The length of the currency symbol must be between 1 and 4 characters", groups = CreateRequest.class, min = 1, max = 4)
    private String symbol;
}
