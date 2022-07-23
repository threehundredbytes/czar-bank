package ru.dreadblade.czarbank.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dreadblade.czarbank.api.model.response.external.CentralBankOfRussiaExchangeRatesResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CentralBankOfRussiaService {
    private static final String CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";
    private static final String EXCHANGE_RATE_DATE_REQUEST_PARAM_NAME = "date_req";
    private static final String EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN = "dd/MM/yyyy";
    private final RestTemplate restTemplate;

    public List<ExchangeRate> getExchangeRatesForCurrenciesByDate(List<Currency> currencies, LocalDate date) {
        String formattedDateRequestParam = date.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)
                .queryParam(EXCHANGE_RATE_DATE_REQUEST_PARAM_NAME, formattedDateRequestParam)
                .encode()
                .toUriString();

        ResponseEntity<CentralBankOfRussiaExchangeRatesResponseDTO> response = restTemplate
                .getForEntity(requestUrl, CentralBankOfRussiaExchangeRatesResponseDTO.class);

        CentralBankOfRussiaExchangeRatesResponseDTO exchangeRatesResponseDTO = response.getBody();

        if (exchangeRatesResponseDTO == null || !exchangeRatesResponseDTO.isValid()) {
            return List.of();
        }

        if (exchangeRatesResponseDTO.getDate().isBefore(date)) {
            exchangeRatesResponseDTO.setDate(date);
        }

        return exchangeRatesResponseDTO.getRates().stream()
                .filter(dto -> currencies.stream().anyMatch(c -> Objects.equals(c.getCode(), dto.getCurrencyCode())))
                .map(dto -> {
                    if (dto.getNominal() > 1) {
                        dto.setRate(dto.getRate().divide(BigDecimal.valueOf(dto.getNominal()), RoundingMode.HALF_EVEN));
                    }

                    Currency currency = currencies.stream()
                            .filter(c -> c.getCode().equals(dto.getCurrencyCode()))
                            .findFirst().orElseThrow(IllegalStateException::new);

                    return ExchangeRate.builder()
                            .currency(currency)
                            .exchangeRate(dto.getRate())
                            .date(exchangeRatesResponseDTO.getDate())
                            .build();
                }).toList();
    }

    public boolean exchangeRateForCurrencyExists(Currency currency) {
        LocalDate date = LocalDate.now();

        String formattedDateRequestParam = date.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)
                .queryParam(EXCHANGE_RATE_DATE_REQUEST_PARAM_NAME, formattedDateRequestParam)
                .encode()
                .toUriString();

        ResponseEntity<CentralBankOfRussiaExchangeRatesResponseDTO> response = restTemplate
                .getForEntity(requestUrl, CentralBankOfRussiaExchangeRatesResponseDTO.class);

        CentralBankOfRussiaExchangeRatesResponseDTO exchangeRatesResponseDTO = response.getBody();

        if (exchangeRatesResponseDTO == null || !exchangeRatesResponseDTO.isValid()) {
            return false;
        }

        return exchangeRatesResponseDTO.getRates().stream()
                .anyMatch(dto -> dto.getCurrencyCode().equals(currency.getCode()));
    }
}
