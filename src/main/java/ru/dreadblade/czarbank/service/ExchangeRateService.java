package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.exception.EntityNotFoundException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ExchangeRateService {
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    @Autowired
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }

    public List<ExchangeRate> findAllLatest() {
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAllLatest();

        if (exchangeRates.isEmpty()) {
            throw new EntityNotFoundException(ExceptionMessage.LATEST_EXCHANGE_RATES_NOT_FOUND);
        }

        return exchangeRates;
    }

    public List<ExchangeRate> findAllByDate(LocalDate date) {
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAllByDate(date);

        if (exchangeRates.isEmpty()) {
            throw new EntityNotFoundException(ExceptionMessage.EXCHANGE_RATES_AT_DATE_NOT_FOUND);
        }

        return exchangeRates;
    }

    public List<ExchangeRate> findAllInTimeSeries(LocalDate startDate, LocalDate endDate) {
        List<ExchangeRate> exchangeRates = exchangeRateRepository.findAllInTimeSeries(startDate, endDate);

        if (exchangeRates.isEmpty() || ChronoUnit.DAYS.between(startDate, endDate) + 1L > exchangeRates.size()) {
            throw new EntityNotFoundException(ExceptionMessage.EXCHANGE_RATES_AT_DATE_NOT_FOUND);
        }

        return exchangeRates;
    }
}
