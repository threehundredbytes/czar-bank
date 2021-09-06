package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateResponseDTO {
    private Long id;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private LocalDate date;
}
