package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.util.UriComponentsBuilder;
import ru.dreadblade.czarbank.api.mapper.ExchangeRateMapper;
import ru.dreadblade.czarbank.api.model.response.ExchangeRateResponseDTO;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest()
@DisplayName("ExchangeRate Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ExchangeRateIntegrationTest extends BaseIntegrationTest {

    private static final String EXCHANGE_RATES_API_URL = "/api/currencies/exchange-rates";
    private static final String LATEST = "/latest";
    private static final String HISTORICAL = "/historical/";
    private static final String TIME_SERIES = "/time-series/";
    private static final String START_DATE_PARAMETER = "start-date";
    private static final String END_DATE_PARAMETER = "end-date";

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    ExchangeRateMapper exchangeRateMapper;

    @Nested
    @DisplayName("findAllLatest() Tests")
    class FindAllLatestTests {
        @Test
        void findAllLatest_isSuccessful() throws Exception {
            List<ExchangeRateResponseDTO> expectedResponseDTOs = exchangeRateRepository.findAllLatest().stream()
                    .map(exchangeRateMapper::entityToResponseDto)
                    .collect(Collectors.toList());

            long expectedSize = currencyRepository.count() - 1L;

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTOs);

            mockMvc.perform(get(EXCHANGE_RATES_API_URL + LATEST)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Rollback
        void findAllLatest_isEmpty() throws Exception {
            exchangeRateRepository.deleteAll();

            final long expectedSize = 0L;

            Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(expectedSize);

            mockMvc.perform(get(EXCHANGE_RATES_API_URL + LATEST)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.LATEST_EXCHANGE_RATES_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("findAllByDate() Tests")
    class FindAllByDateTests {
        @Test
        void findAllByDate_isSuccessful() throws Exception {
            LocalDate expectedDate = LocalDate.of(2021, 9, 1);

            String dateStr = expectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<ExchangeRateResponseDTO> expectedResponseDTOs = exchangeRateRepository.findAllByDate(expectedDate).stream()
                    .map(exchangeRateMapper::entityToResponseDto)
                    .collect(Collectors.toList());

            long expectedSize = currencyRepository.count() - 1L;

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTOs);

            mockMvc.perform(get(EXCHANGE_RATES_API_URL + HISTORICAL + dateStr)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        void findAllByDate_dataDoesNotExistOnTheGivenDate_isFailed() throws Exception {
            LocalDate expectedDate = LocalDate.of(2010, 6, 10);

            String dateStr = expectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            mockMvc.perform(get(EXCHANGE_RATES_API_URL + HISTORICAL + dateStr)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.EXCHANGE_RATES_AT_DATE_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("findAllInTimeSeries() Tests")
    class FindAllInTimeSeriesTests {
        @Test
        void findAllInTimeSeries_isSuccessful() throws Exception {
            LocalDate startDate = LocalDate.of(2021, 9, 1);
            LocalDate endDate = LocalDate.of(2021, 9, 5);

            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String URL = UriComponentsBuilder.fromUriString(EXCHANGE_RATES_API_URL + TIME_SERIES)
                    .queryParam(START_DATE_PARAMETER, startDateStr)
                    .queryParam(END_DATE_PARAMETER, endDateStr)
                    .encode()
                    .build()
                    .toUriString();

            List<ExchangeRateResponseDTO> expectedResponseDTOs = exchangeRateRepository.findAllInTimeSeries(startDate, endDate).stream()
                    .map(exchangeRateMapper::entityToResponseDto)
                    .collect(Collectors.toList());

            long expectedSize = (currencyRepository.count() - 1L) * (ChronoUnit.DAYS.between(startDate, endDate) + 1L);

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTOs);

            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        void findAllInTimeSeries_dataDoesNotExistOnTheGivenDate_isFailed() throws Exception {
            LocalDate startDate = LocalDate.of(2009, 6, 10);
            LocalDate endDate = LocalDate.of(2010, 6, 10);

            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String URL = UriComponentsBuilder.fromUriString(EXCHANGE_RATES_API_URL + TIME_SERIES)
                    .queryParam(START_DATE_PARAMETER, startDateStr)
                    .queryParam(END_DATE_PARAMETER, endDateStr)
                    .encode()
                    .build()
                    .toUriString();

            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.EXCHANGE_RATES_AT_DATE_NOT_FOUND.getMessage()));
        }

        @Test
        void findAllInTimeSeries_dataIsNotCompleteOnTheGivenDate_isFailed() throws Exception {
            LocalDate startDate = LocalDate.of(2009, 6, 10);
            LocalDate endDate = LocalDate.of(2021, 9, 4);

            String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String URL = UriComponentsBuilder.fromUriString(EXCHANGE_RATES_API_URL + TIME_SERIES)
                    .queryParam(START_DATE_PARAMETER, startDateStr)
                    .queryParam(END_DATE_PARAMETER, endDateStr)
                    .encode()
                    .build()
                    .toUriString();

            mockMvc.perform(get(URL).accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.EXCHANGE_RATES_AT_DATE_NOT_FOUND.getMessage()));
        }
    }
}
