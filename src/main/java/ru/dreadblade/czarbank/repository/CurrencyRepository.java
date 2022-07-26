package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.dreadblade.czarbank.domain.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    @Query("select c from Currency as c where c.code <> 'RUB'")
    List<Currency> findAllForeignCurrencies();

    Optional<Currency> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsBySymbol(String symbol);
}
