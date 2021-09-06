package ru.dreadblade.czarbank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.dreadblade.czarbank.api.model.response.external.CbrExchangeRatesResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.exception.EntityNotFoundException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FetchExchangeRateService {

    private static final String EXCHANGE_RATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    public FetchExchangeRateService(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    @Scheduled(fixedRateString = "${czar-bank.currency.exchange-rate.update-rate-in-millis:3600000}")
    public void fetchExchangeRatesFromCentralBankOfRussia() {
        ResponseEntity<CbrExchangeRatesResponseDTO> response = restTemplate
                .getForEntity(EXCHANGE_RATE_API_URL, CbrExchangeRatesResponseDTO.class);

        CbrExchangeRatesResponseDTO responseDTO = response.getBody();

        if (!response.getStatusCode().is2xxSuccessful() || responseDTO == null || responseDTO.getRates() == null ||
                responseDTO.getRates().isEmpty()) {
            log.error("Error when fetching currency exchange rates from the API of the Central Bank of Russia");
            return;
        }

        if (responseDTO.getDate().isBefore(LocalDate.now())) {
            responseDTO.setDate(LocalDate.now());
        }

        List<String> usedCurrencies = currencyRepository.findAll().stream()
                .map(Currency::getCode)
                .collect(Collectors.toList());

        List<ExchangeRate> exchangeRates = responseDTO.getRates().stream()
                .filter(dto -> usedCurrencies.contains(dto.getCurrencyCode()))
                .map(dto -> {
                    if (dto.getNominal() > 1) {
                        dto.setRate(dto.getRate().divide(BigDecimal.valueOf(dto.getNominal()), RoundingMode.HALF_UP));
                    }

                    Currency currency = currencyRepository.findByCode(dto.getCurrencyCode())
                            .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.CURRENCY_NOT_FOUND));

                    return ExchangeRate.builder()
                            .currency(currency)
                            .exchangeRate(dto.getRate())
                            .date(responseDTO.getDate())
                            .build();
                })
                .collect(Collectors.toList());

        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRateRepository.existsByCurrencyAndDate(exchangeRate.getCurrency(), exchangeRate.getDate())) {
                exchangeRateRepository.deleteByCurrencyAndDate(exchangeRate.getCurrency(), exchangeRate.getDate());
            }
        }

        exchangeRateRepository.saveAll(exchangeRates);

        log.info("Fetching currency exchange rates from the API of the Central Bank of Russia completed successfully");
    }
}