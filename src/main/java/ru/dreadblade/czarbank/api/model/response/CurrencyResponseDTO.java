package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyResponseDTO {
    private Long id;
    private String code;
    private String symbol;
}
