package ru.dreadblade.czarbank.domain;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ExchangeRate extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    /**
     * Exchange rate against the Russian Ruble
     */
    private BigDecimal exchangeRate;

    private LocalDate date;
}
