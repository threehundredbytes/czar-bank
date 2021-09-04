package ru.dreadblade.czarbank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
}
