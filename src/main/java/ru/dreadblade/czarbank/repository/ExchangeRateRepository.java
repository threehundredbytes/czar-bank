package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;

import java.time.LocalDate;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    boolean existsByCurrencyAndDate(Currency currency, LocalDate date);
    void deleteByCurrencyAndDate(Currency currency, LocalDate date);
}
