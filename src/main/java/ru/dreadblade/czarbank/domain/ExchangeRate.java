package ru.dreadblade.czarbank.domain;

import lombok.*;
import ru.dreadblade.czarbank.domain.key.ExchangeRateCompositeKey;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(ExchangeRateCompositeKey.class)
public class ExchangeRate extends BaseEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private LocalDate date;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    /**
     * Exchange rate against the Russian Ruble
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal exchangeRate;
}
