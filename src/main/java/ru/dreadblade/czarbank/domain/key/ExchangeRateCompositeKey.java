package ru.dreadblade.czarbank.domain.key;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateCompositeKey implements Serializable {
    private LocalDate date;
    private Long currency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeRateCompositeKey that)) return false;
        return date.equals(that.date) && currency.equals(that.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, currency);
    }
}