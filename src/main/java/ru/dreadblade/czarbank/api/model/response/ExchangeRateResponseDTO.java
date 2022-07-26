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
    private LocalDate date;
    private Long currencyId;
    private BigDecimal exchangeRate;
}
