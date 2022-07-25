package ru.dreadblade.czarbank.service.task.startup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.external.CentralBankOfRussiaService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoadExchangeRatesHistoryStartupTask implements StartupTask {
    private final CentralBankOfRussiaService centralBankOfRussiaService;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    @Value("#{T(java.time.LocalDate).parse('${czar-bank.exchange-rate.history.load-from-date:2012-01-01}')}")
    private LocalDate loadHistoryFromDate;

    @Override
    public void run(ApplicationArguments args) {
        run();
    }

    @Override
    public void run() {
        List<Currency> foreignCurrencies = currencyRepository.findAllForeignCurrencies();

        LocalDate loadHistoryToDate = LocalDate.now();

        long requiredExchangeRateCount = (ChronoUnit.DAYS.between(loadHistoryFromDate, loadHistoryToDate) + 1) *
                foreignCurrencies.size();

        if (exchangeRateRepository.count() != requiredExchangeRateCount) {
            exchangeRateRepository.deleteAll();

            foreignCurrencies.forEach(currency -> {
                List<ExchangeRate> exchangeRates = centralBankOfRussiaService
                        .getExchangeRatesForCurrencyBetweenDates(currency, loadHistoryFromDate, loadHistoryToDate);

                exchangeRateRepository.saveAll(exchangeRates);

                log.trace("Loaded exchange rates history ({})", currency.getSymbol());
            });

            log.info("Loading the history of exchange rates from the API of the Central Bank of the Russian Federation has been successfully completed");
        }
    }
}
