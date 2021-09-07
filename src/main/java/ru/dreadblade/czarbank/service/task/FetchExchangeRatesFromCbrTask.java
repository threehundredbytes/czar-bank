package ru.dreadblade.czarbank.service.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
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

@Component
public class FetchExchangeRatesFromCbrTask implements Task {

    private static final String EXCHANGE_RATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    @Autowired
    public FetchExchangeRatesFromCbrTask(ExchangeRateRepository exchangeRateRepository, CurrencyRepository currencyRepository, RestTemplate restTemplate) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
    }

    @Transactional
    @Override
    public boolean execute() {
        ResponseEntity<CbrExchangeRatesResponseDTO> response;

        try {
            response = restTemplate.getForEntity(EXCHANGE_RATE_API_URL, CbrExchangeRatesResponseDTO.class);
        } catch (RestClientException e) {
            return false;
        }

        CbrExchangeRatesResponseDTO responseDTO = response.getBody();

        if (responseDTO == null || responseDTO.getRates() == null || responseDTO.getRates().isEmpty()) {
            return false;
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
                        dto.setRate(dto.getRate().divide(BigDecimal.valueOf(dto.getNominal()), RoundingMode.UP));
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

        return true;
    }
}