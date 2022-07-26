package ru.dreadblade.czarbank.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.external.CentralBankOfRussiaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {
    public static final String BASE_CURRENCY = "RUB";

    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CentralBankOfRussiaService centralBankOfRussiaService;

    @Value("#{T(java.time.LocalDate).parse('${czar-bank.exchange-rate.history.load-from-date:2012-01-01}')}")
    private LocalDate loadHistoryFromDate;

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

        Currency currency = Currency.builder()
                .code(currencyCode)
                .symbol(currencySymbol)
                .build();

        if (!centralBankOfRussiaService.exchangeRateForCurrencyExists(currency, loadHistoryFromDate)) {
            throw new CzarBankException(ExceptionMessage.UNSUPPORTED_CURRENCY);
        }

        currency = currencyRepository.save(currency);

        LocalDate today = LocalDate.now();

        exchangeRateRepository.saveAll(
                centralBankOfRussiaService.getExchangeRatesForCurrencyBetweenDates(currency, loadHistoryFromDate, today)
        );

        return currency;
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
        return exchangeRateRepository.findAllLatest().stream()
                .filter(exchangeRate -> exchangeRate.getCurrency().getCode().equals(currency.getCode()))
                .findFirst()
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.LATEST_EXCHANGE_RATES_NOT_FOUND))
                .getExchangeRate();
    }
}
