package ru.dreadblade.czarbank.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
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
