package ru.dreadblade.czarbank.service.task.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.external.CentralBankOfRussiaService;
import ru.dreadblade.czarbank.service.task.Task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class GetExchangeRatesFromCentralBankOfRussiaTask implements Task {
    private final CentralBankOfRussiaService centralBankOfRussiaService;
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;

    @Scheduled(fixedRateString = "#{${czar-bank.currency.exchange-rate.update-rate-seconds:86400}}", timeUnit = TimeUnit.SECONDS)
    @Override
    public void run() {
        List<Currency> currencies = currencyRepository.findAll();
        LocalDate date = LocalDate.now();

        try {
            List<ExchangeRate> exchangeRates = centralBankOfRussiaService.getExchangeRatesForCurrenciesByDate(currencies, date);

            if (exchangeRates == null || exchangeRates.isEmpty()) {
                throw new IllegalStateException();
            }

            if (exchangeRateRepository.findAllLatest().stream().anyMatch(exchangeRate -> exchangeRate.getDate().equals(date))) {
                for (ExchangeRate exchangeRate : exchangeRates) {
                    Optional<ExchangeRate> existingExchangeRateOptional = exchangeRateRepository.findByCurrencyAndDate(exchangeRate.getCurrency(), date);

                    if (existingExchangeRateOptional.isPresent()) {
                        ExchangeRate existingExchangeRate = existingExchangeRateOptional.get();

                        BigDecimal exchangeRateValue = exchangeRate.getExchangeRate().setScale(2, RoundingMode.HALF_EVEN);

                        if (exchangeRateValue.compareTo(existingExchangeRate.getExchangeRate()) != 0) {
                            existingExchangeRate.setExchangeRate(exchangeRate.getExchangeRate());

                            exchangeRateRepository.save(existingExchangeRate);
                        }
                    } else {
                        exchangeRateRepository.save(exchangeRate);
                    }
                }
            } else {
                exchangeRateRepository.saveAll(exchangeRates);
            }

            log.info("Loading exchange rates from the API of the Central Bank of Russian Federation has been successfully completed");
        } catch (Exception e) {
            log.error("Error when loading exchange rates from the API of the Central Bank of Russian Federation");
        }
    }
}