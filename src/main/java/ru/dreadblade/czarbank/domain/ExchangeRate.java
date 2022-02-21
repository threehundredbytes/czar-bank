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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exchange_rate_id_sequence")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    private Currency currency;

    /**
     * Exchange rate against the Russian Ruble
     */
    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal exchangeRate;

    @Column(nullable = false, updatable = false)
    private LocalDate date;
}
