package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.domain.key.ExchangeRateCompositeKey;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, ExchangeRateCompositeKey> {
    @Query("select e from ExchangeRate as e where e.date in (select max(date) from ExchangeRate)")
    List<ExchangeRate> findAllLatest();

    List<ExchangeRate> findAllByDate(LocalDate date);

    @Query("select e from ExchangeRate as e where e.date between :start_date and :end_date order by e.date asc")
    List<ExchangeRate> findAllInTimeSeries(@Param("start_date") LocalDate startDate, @Param("end_date") LocalDate endDate);

    Optional<ExchangeRate> findByCurrencyAndDate(Currency currency, LocalDate date);
}