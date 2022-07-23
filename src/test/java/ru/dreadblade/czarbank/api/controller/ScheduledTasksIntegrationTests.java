package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
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
import ru.dreadblade.czarbank.api.model.response.external.CentralBankOfRussiaExchangeRatesResponseDTO;
import ru.dreadblade.czarbank.domain.ExchangeRate;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.task.scheduled.GetExchangeRatesFromCentralBankOfRussiaTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(properties = {
        "czar-bank.currency.exchange-rate.update-rate-in-millis=5000",
        "czar-bank.scheduling.enabled=false"
})
@DisplayName("ScheduledTasks Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ScheduledTasksIntegrationTests extends BaseIntegrationTest {
    private static final String CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL = "https://www.cbr.ru/scripts/XML_daily.asp";

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    GetExchangeRatesFromCentralBankOfRussiaTask getExchangeRatesFromCentralBankOfRussiaTask;

    XmlMapper xmlMapper = XmlMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Nested
    @DisplayName("Get exchange rates from the Central Bank Of Russia task tests")
    class getExchangeRatesFromCentralBankOfRussiaTaskTests {
        @Test
        @Rollback
        void getExchangeRatesFromCentralBankOfRussiaTask_isSuccessful() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            List<CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO> expectedRates = List.of(
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("12.34567"))
                            .build(),
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("123.45678"))
                            .build()
            );

            String responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(expectedRates)
                    .date(LocalDate.now())
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            List<ExchangeRate> actualRates = exchangeRateRepository.findAllLatest();

            Assertions.assertThat(actualRates).isNotEmpty();
            Assertions.assertThat(actualRates).allMatch(rate -> rate.getDate().isEqual(LocalDate.now()));

            Assertions.assertThat(actualRates.get(0).getExchangeRate())
                    .isEqualByComparingTo(expectedRates.get(0).getRate().setScale(2, RoundingMode.HALF_EVEN));
            Assertions.assertThat(actualRates.get(0).getCurrency().getCode())
                    .isEqualTo(expectedRates.get(0).getCurrencyCode());

            Assertions.assertThat(actualRates.get(1).getExchangeRate())
                    .isEqualByComparingTo(expectedRates.get(1).getRate().setScale(2, RoundingMode.HALF_EVEN));
            Assertions.assertThat(actualRates.get(1).getCurrency().getCode())
                    .isEqualTo(expectedRates.get(1).getCurrencyCode());
        }

        @Test
        @Rollback
        void getExchangeRatesFromCentralBankOfRussiaTask_doesntDeleteExistingRates_isSuccessful() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            List<CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO> expectedRates = List.of(
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("12.34567"))
                            .build(),
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("123.45678"))
                            .build()
            );

            String responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(expectedRates)
                    .date(LocalDate.now())
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));


            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();
            mockServer.reset();

            Long expectedCount = exchangeRateRepository.count();
            List<ExchangeRate> expectedExchangeRates = exchangeRateRepository.findAllLatest();
            List<Long> expectedExchangeRateIds = expectedExchangeRates.stream()
                    .map(ExchangeRate::getId)
                    .toList();
            List<BigDecimal> expectedExchangeRateValues = expectedExchangeRates.stream()
                    .map(ExchangeRate::getExchangeRate)
                    .toList();

            expectedRates = List.of(
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("45.678"))
                            .build(),
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("56.789"))
                            .build()
            );

            responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(expectedRates)
                    .date(LocalDate.now())
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(expectedCount);
            Assertions.assertThat(exchangeRateRepository.findAllLatest().stream()
                    .map(ExchangeRate::getId)
                    .toList()).containsExactlyInAnyOrderElementsOf(expectedExchangeRateIds);
            Assertions.assertThat(exchangeRateRepository.findAllLatest().stream()
                    .map(ExchangeRate::getExchangeRate)
                    .toList()).doesNotContainAnyElementsOf(expectedExchangeRateValues);
        }
        @Test
        @Rollback
        void getExchangeRatesFromCentralBankOfRussiaTask_responseDateIsBeforeToday_isSuccessful() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            List<CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO> rates = List.of(
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(2L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("12.34567"))
                            .build(),
                    CentralBankOfRussiaExchangeRatesResponseDTO.CbrExchangeRateResponseDTO.builder()
                            .currencyCode(currencyRepository.findById(3L).orElseThrow().getCode())
                            .nominal(1L)
                            .rate(new BigDecimal("123.45678"))
                            .build()
            );

            LocalDate expectedDate = LocalDate.now();
            LocalDate responseDate = expectedDate.minusDays(1);

            String responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(rates)
                    .date(responseDate)
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.OK)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            List<ExchangeRate> addedRates = exchangeRateRepository.findAllLatest();

            Assertions.assertThat(addedRates).isNotEmpty();
            Assertions.assertThat(addedRates).allMatch(rate -> rate.getDate().isEqual(expectedDate));

            Assertions.assertThat(addedRates.get(0).getExchangeRate())
                    .isEqualByComparingTo(rates.get(0).getRate().setScale(2, RoundingMode.HALF_EVEN));
            Assertions.assertThat(addedRates.get(0).getCurrency().getCode())
                    .isEqualTo(rates.get(0).getCurrencyCode());

            Assertions.assertThat(addedRates.get(1).getExchangeRate())
                    .isEqualByComparingTo(rates.get(1).getRate().setScale(2, RoundingMode.HALF_EVEN));
            Assertions.assertThat(addedRates.get(1).getCurrency().getCode())
                    .isEqualTo(rates.get(1).getCurrencyCode());
        }

        @Test
        void getExchangeRatesFromCentralBankOfRussiaTask_responseStatusIsInvalid_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = xmlMapper.writeValueAsString(new CentralBankOfRussiaExchangeRatesResponseDTO());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            Long expectedCount = exchangeRateRepository.count();

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(expectedCount);
        }

        @Test
        void getExchangeRatesFromCentralBankOfRussiaTask_ratesIsNull_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(null)
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            Long expectedCount = exchangeRateRepository.count();

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(expectedCount);
        }

        @Test
        void getExchangeRatesFromCentralBankOfRussiaTask_ratesIsEmpty_isFailed() throws Exception {
            MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);

            String responseBody = xmlMapper.writeValueAsString(CentralBankOfRussiaExchangeRatesResponseDTO.builder()
                    .rates(new ArrayList<>())
                    .build());

            mockServer.expect(ExpectedCount.once(), requestTo(Matchers.containsString(CENTRAL_BANK_OF_RUSSIA_EXCHANGE_RATE_API_URL)))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_XML)
                            .body(responseBody));

            Long expectedCount = exchangeRateRepository.count();

            getExchangeRatesFromCentralBankOfRussiaTask.run();
            mockServer.verify();

            Assertions.assertThat(exchangeRateRepository.count()).isEqualTo(expectedCount);
        }
    }
}
