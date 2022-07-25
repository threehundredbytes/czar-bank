package ru.dreadblade.czarbank.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dreadblade.czarbank.api.model.response.external.CentralBankOfRussiaExchangeRatesBetweenDatesResponseDTO;
import ru.dreadblade.czarbank.api.model.response.external.CentralBankOfRussiaExchangeRatesResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.domain.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CentralBankOfRussiaService {
    private static final String CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_ON_DATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";
    private static final String CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_BETWEEN_DATES_API_URL = "https://www.cbr.ru/scripts/XML_dynamic.asp";

    private static final String EXCHANGE_RATE_DATE_REQUEST_PARAM_NAME = "date_req";
    private static final String EXCHANGE_RATE_START_DATE_REQUEST_PARAM_NAME = "date_req1";
    private static final String EXCHANGE_RATE_END_DATE_REQUEST_PARAM_NAME = "date_req2";
    private static final String EXCHANGE_RATE_UNIQUE_CURRENCY_CODE_REQUEST_PARAM_NAME = "VAL_NM_RQ";

    private static final String EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN = "dd/MM/yyyy";

    private final RestTemplate restTemplate;

    public List<ExchangeRate> getExchangeRatesForCurrenciesByDate(List<Currency> currencies, LocalDate date) {
        String formattedDateRequestParam = date.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_ON_DATE_API_URL)
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

    public List<ExchangeRate> getExchangeRatesForCurrencyBetweenDates(Currency currency, LocalDate startDate, LocalDate endDate) {
        String formattedStartDateRequestParam = startDate.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));
        String formattedEndDateRequestParam = endDate.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_BETWEEN_DATES_API_URL)
                .queryParam(EXCHANGE_RATE_START_DATE_REQUEST_PARAM_NAME, formattedStartDateRequestParam)
                .queryParam(EXCHANGE_RATE_END_DATE_REQUEST_PARAM_NAME, formattedEndDateRequestParam)
                .queryParam(EXCHANGE_RATE_UNIQUE_CURRENCY_CODE_REQUEST_PARAM_NAME, getUniqueCurrencyCodeForCurrency(currency))
                .encode()
                .toUriString();

        ResponseEntity<CentralBankOfRussiaExchangeRatesBetweenDatesResponseDTO> response = restTemplate
                .getForEntity(requestUrl, CentralBankOfRussiaExchangeRatesBetweenDatesResponseDTO.class);

        var exchangeRatesResponseDTO = response.getBody();

        if (exchangeRatesResponseDTO == null || !exchangeRatesResponseDTO.isValid()) {
            return List.of();
        }

        LocalDate currentDate = startDate;
        List<ExchangeRate> result = new ArrayList<>();

        var exchangeRateDTOs = exchangeRatesResponseDTO.getRates();
        int exchangeRatesCount = exchangeRateDTOs.size();

        exchangeRateDTOs.sort(Comparator.comparing(e -> e.getDate()));

        for (int i = 0; i < exchangeRatesCount; i++) {
            var rateDTO = exchangeRateDTOs.get(i);

            if (rateDTO.getNominal() > 1) {
                rateDTO.setRate(rateDTO.getRate().divide(BigDecimal.valueOf(rateDTO.getNominal()), RoundingMode.HALF_EVEN));
            }

            while (i != exchangeRatesCount - 1 && currentDate.isBefore(exchangeRateDTOs.get(i + 1).getDate()) || i == exchangeRatesCount - 1 && currentDate.isBefore(endDate.plusDays(1))) {
                result.add(ExchangeRate.builder()
                        .currency(currency)
                        .exchangeRate(rateDTO.getRate())
                        .date(currentDate)
                        .build());

                currentDate = currentDate.plusDays(1);
            }
        }

        return result;
    }

    public boolean exchangeRateForCurrencyExists(Currency currency, LocalDate fromDate) {
        String formattedDateRequestParam = fromDate.format(DateTimeFormatter.ofPattern(EXCHANGE_RATE_DATE_REQUEST_PARAM_PATTERN));

        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_ON_DATE_API_URL)
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

    private String getUniqueCurrencyCodeForCurrency(Currency currency) {
        String requestUrl = UriComponentsBuilder.fromHttpUrl(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATES_ON_DATE_API_URL)
                .encode()
                .toUriString();

        ResponseEntity<CentralBankOfRussiaExchangeRatesResponseDTO> response = restTemplate
                .getForEntity(requestUrl, CentralBankOfRussiaExchangeRatesResponseDTO.class);

        CentralBankOfRussiaExchangeRatesResponseDTO exchangeRatesResponseDTO = response.getBody();

        if (exchangeRatesResponseDTO == null || !exchangeRatesResponseDTO.isValid()) {
            return null;
        }

        return exchangeRatesResponseDTO.getRates().stream()
                .filter(dto -> dto.getCurrencyCode().equals(currency.getCode()))
                .map(dto -> dto.getUniqueCurrencyCode())
                .findAny().orElseThrow(IllegalStateException::new);
    }
}
