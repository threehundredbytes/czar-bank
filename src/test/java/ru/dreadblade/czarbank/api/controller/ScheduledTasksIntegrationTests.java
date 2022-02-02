package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.dreadblade.czarbank.api.model.response.external.CbrExchangeRatesResponseDTO;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.task.FetchExchangeRatesFromCbrTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(properties = { "czar-bank.currency.exchange-rate.update-rate-in-millis=5000" })
@DisplayName("ScheduledTasks Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ScheduledTasksIntegrationTests extends BaseIntegrationTest {
    private static final String CBR_EXCHANGE_RATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    FetchExchangeRatesFromCbrTask fetchExchangeRatesFromCbrTask;

    @Nested
    @DisplayName("Fetch exchange rates from the Central Bank Of Russia Tests")
    class FetchExchangeRatesFromCentralBankOfRussiaTests {
        @Test
        @Rollback
        void fetchExchangeRatesFromCentralBankOfRussia_isSuccessful() {
            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isTrue();
        }

        @Test
        @Rollback
        void fetchExchangeRatesFromCentralBankOfRussia_responseDateIsBeforeToday_isSuccessful() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            List<CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO> rates = List.of(
                    CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("12.34567"))
                            .build(),
                    CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("123.45678"))
                            .build()
            );

            LocalDate yesterday = LocalDate.now().minusDays(1);

            String responseBody = objectMapper.writeValueAsString(CbrExchangeRatesResponseDTO.builder()
                    .rates(rates)
                    .date(yesterday)
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(CBR_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody));

            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isTrue();

            List<ExchangeRate> addedRates = exchangeRateRepository.findAllLatest();

            Assertions.assertThat(addedRates).isNotEmpty();
            Assertions.assertThat(addedRates.get(0).getExchangeRate())
                    .isEqualTo(rates.get(0).getRate().setScale(2, RoundingMode.UP));
            Assertions.assertThat(addedRates.get(0).getCurrency().getCode())
                    .isEqualTo(rates.get(0).getCurrencyCode());
            Assertions.assertThat(addedRates.get(1).getExchangeRate())
                    .isEqualTo(rates.get(1).getRate().setScale(2, RoundingMode.UP));
            Assertions.assertThat(addedRates.get(1).getCurrency().getCode())
                    .isEqualTo(rates.get(1).getCurrencyCode());
        }

        @Test
        @Rollback
        void fetchExchangeRatesFromCentralBankOfRussia_rewritesExistingData_isSuccessful() throws Exception {
            List<ExchangeRate> addedRates = exchangeRateRepository.findAllLatest();

            Assertions.assertThat(addedRates).isNotEmpty();

            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            List<CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO> rates = List.of(
                    CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("1234.56789"))
                            .build(),
                    CbrExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("1234.56789"))
                            .build()
            );

            LocalDate expectedDate = LocalDate.now();

            String responseBody = objectMapper.writeValueAsString(CbrExchangeRatesResponseDTO.builder()
                    .rates(rates)
                    .date(expectedDate)
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(CBR_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody));

            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isTrue();

            addedRates = exchangeRateRepository.findAllLatest();

            Assertions.assertThat(addedRates).isNotEmpty();
            Assertions.assertThat(addedRates.get(0).getExchangeRate())
                    .isEqualTo(rates.get(0).getRate().setScale(2, RoundingMode.UP));
            Assertions.assertThat(addedRates.get(0).getCurrency().getCode())
                    .isEqualTo(rates.get(0).getCurrencyCode());
            Assertions.assertThat(addedRates.get(1).getExchangeRate())
                    .isEqualTo(rates.get(1).getRate().setScale(2, RoundingMode.UP));
            Assertions.assertThat(addedRates.get(1).getCurrency().getCode())
                    .isEqualTo(rates.get(1).getCurrencyCode());
        }

        @Test
        void fetchExchangeRatesFromCentralBankOfRussia_responseStatusIsInvalid_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = objectMapper.writeValueAsString(new CbrExchangeRatesResponseDTO());

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(CBR_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody));

            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isFalse();
        }

        @Test
        void fetchExchangeRatesFromCentralBankOfRussia_ratesIsNull_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = objectMapper.writeValueAsString(CbrExchangeRatesResponseDTO.builder()
                    .rates(null)
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(CBR_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody));

            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isFalse();
        }

        @Test
        void fetchExchangeRatesFromCentralBankOfRussia_ratesIsEmpty_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = objectMapper.writeValueAsString(CbrExchangeRatesResponseDTO.builder()
                    .rates(new ArrayList<>())
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(new URI(CBR_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(responseBody));

            Assertions.assertThat(fetchExchangeRatesFromCbrTask.execute()).isFalse();
        }
    }
}
