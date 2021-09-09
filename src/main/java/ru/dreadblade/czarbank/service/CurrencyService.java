package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.exception.UnsupportedCurrencyException;
import ru.dreadblade.czarbank.exception.UniqueFieldAlreadyExistsException;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.task.Task;

import java.util.List;

@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final Task fetchExchangeRatesFromCbrTask;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository, ExchangeRateRepository exchangeRateRepository, Task fetchExchangeRatesFromCbrTask) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.fetchExchangeRatesFromCbrTask = fetchExchangeRatesFromCbrTask;
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    public Currency createCurrency(String currencyCode, String currencySymbol) {
        if (currencyRepository.existsByCode(currencyCode)) {
            throw new UniqueFieldAlreadyExistsException(ExceptionMessage.CURRENCY_CODE_ALREADY_EXISTS);
        }

        if (currencyRepository.existsBySymbol(currencySymbol)) {
            throw new UniqueFieldAlreadyExistsException(ExceptionMessage.CURRENCY_SYMBOL_ALREADY_EXISTS);
        }

        Currency createdCurrency = currencyRepository.save(Currency.builder()
                .code(currencyCode)
                .symbol(currencySymbol)
                .build());

        fetchExchangeRatesFromCbrTask.execute();

        boolean exchangeRateForCurrencyExists = exchangeRateRepository.findAllLatest()
                .stream()
                .filter(exchangeRate -> exchangeRate.getCurrency().getCode().equals(createdCurrency.getCode()))
                .count() == 1;

        if (!exchangeRateForCurrencyExists) {
            currencyRepository.deleteById(createdCurrency.getId());
            throw new UnsupportedCurrencyException(ExceptionMessage.UNSUPPORTED_CURRENCY);
        }

        return createdCurrency;
    }
}
