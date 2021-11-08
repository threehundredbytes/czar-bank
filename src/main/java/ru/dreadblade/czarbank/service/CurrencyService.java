package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.service.task.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CurrencyService {
    public static final String BASE_CURRENCY = "RUB";

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateService exchangeRateService;
    private final Task fetchExchangeRatesFromCbrTask;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository, ExchangeRateService exchangeRateService, Task fetchExchangeRatesFromCbrTask) {
        this.currencyRepository = currencyRepository;
        this.exchangeRateService = exchangeRateService;
        this.fetchExchangeRatesFromCbrTask = fetchExchangeRatesFromCbrTask;
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }

    public Currency createCurrency(String currencyCode, String currencySymbol) {
        if (currencyRepository.existsByCode(currencyCode)) {
            throw new CzarBankException(ExceptionMessage.CURRENCY_CODE_ALREADY_EXISTS);
        }

        if (currencyRepository.existsBySymbol(currencySymbol)) {
            throw new CzarBankException(ExceptionMessage.CURRENCY_SYMBOL_ALREADY_EXISTS);
        }

        Currency createdCurrency = currencyRepository.save(Currency.builder()
                .code(currencyCode)
                .symbol(currencySymbol)
                .build());

        fetchExchangeRatesFromCbrTask.execute();

        boolean exchangeRateForCurrencyExists = exchangeRateService.findAllLatest()
                .stream()
                .anyMatch(exchangeRate -> exchangeRate.getCurrency().getCode().equals(createdCurrency.getCode()));

        if (!exchangeRateForCurrencyExists) {
            currencyRepository.deleteById(createdCurrency.getId());
            throw new CzarBankException(ExceptionMessage.UNSUPPORTED_CURRENCY);
        }

        return createdCurrency;
    }

    public BigDecimal exchangeCurrency(Currency source, BigDecimal amount, Currency target) {
        if (source.getCode().equals(target.getCode())) {
            return amount;
        }

        if (source.getCode().equals(BASE_CURRENCY)) {
            BigDecimal rate = getExchangeRateByCurrency(target);
            return amount.divide(rate, RoundingMode.HALF_EVEN);
        }

        if (target.getCode().equals(BASE_CURRENCY)) {
            BigDecimal rate = getExchangeRateByCurrency(source);
            return amount.multiply(rate);
        }

        BigDecimal rateToRub = getExchangeRateByCurrency(source);

        BigDecimal amountInRub = amount.multiply(rateToRub);

        BigDecimal rateToTarget = getExchangeRateByCurrency(target);

        return amountInRub.divide(rateToTarget, RoundingMode.HALF_EVEN);
    }

    private BigDecimal getExchangeRateByCurrency(Currency currency) {
        return exchangeRateService.findAllLatest().stream()
                .filter(exchangeRate -> exchangeRate.getCurrency().getCode().equals(currency.getCode()))
                .findFirst()
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.LATEST_EXCHANGE_RATES_NOT_FOUND))
                .getExchangeRate();
    }
}
