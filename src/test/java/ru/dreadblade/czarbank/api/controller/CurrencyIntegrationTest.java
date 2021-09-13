package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import ru.dreadblade.czarbank.api.mapper.CurrencyMapper;
import ru.dreadblade.czarbank.api.model.request.CurrencyRequestDTO;
import ru.dreadblade.czarbank.api.model.response.CurrencyResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.BankAccountService;
import ru.dreadblade.czarbank.service.CurrencyService;
import ru.dreadblade.czarbank.service.ExchangeRateService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Currency Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CurrencyIntegrationTest extends BaseIntegrationTest {
    public static final String CURRENCIES_API_URL = "/api/currencies";

    @Autowired
    BankAccountService bankAccountService;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    ExchangeRateService exchangeRateService;

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Autowired
    CurrencyService currencyService;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrencyMapper currencyMapper;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            long expectedSize = currencyRepository.count();

            List<CurrencyResponseDTO> expectedDTOs = currencyRepository.findAll().stream()
                    .map(currencyMapper::entityToResponseDTO)
                    .collect(Collectors.toList());

            String expectedResponse = objectMapper.writeValueAsString(expectedDTOs);

            mockMvc.perform(get(CURRENCIES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Rollback
        void findAll_isEmpty() throws Exception {
            bankAccountRepository.deleteAll();
            exchangeRateRepository.deleteAll();
            currencyRepository.deleteAll();

            long expectedSize = 0L;

            Assertions.assertThat(currencyRepository.count()).isEqualTo(expectedSize);

            mockMvc.perform(get(CURRENCIES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))));
        }
    }

    @Nested
    @DisplayName("createCurrency() Tests")
    class createCurrencyTests {
        @Test
        @Rollback
        void createCurrency_isSuccessful() throws Exception {
            CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                    .code("SEK")
                    .symbol("kr")
                    .build();

            Assertions.assertThat(currencyRepository.existsByCode(requestDTO.getCode())).isFalse();
            Assertions.assertThat(currencyRepository.existsBySymbol(requestDTO.getSymbol())).isFalse();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(CURRENCIES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString(CURRENCIES_API_URL)))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.code").value(requestDTO.getCode()))
                    .andExpect(jsonPath("$.symbol").value(requestDTO.getSymbol()));
        }

        @Test
        void createCurrency_currencyCodeAlreadyExists_isFailed() throws Exception {
            CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                    .code("RUB")
                    .symbol("₽")
                    .build();

            Assertions.assertThat(currencyRepository.existsByCode(requestDTO.getCode())).isTrue();
            Assertions.assertThat(currencyRepository.existsBySymbol(requestDTO.getSymbol())).isTrue();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(CURRENCIES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.CURRENCY_CODE_ALREADY_EXISTS.getMessage()));
        }

        @Test
        void createCurrency_currencySymbolAlreadyExists_isFailed() throws Exception {
            CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                    .code("newUSD")
                    .symbol("$")
                    .build();

            Assertions.assertThat(currencyRepository.existsByCode(requestDTO.getCode())).isFalse();
            Assertions.assertThat(currencyRepository.existsBySymbol(requestDTO.getSymbol())).isTrue();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(CURRENCIES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.CURRENCY_SYMBOL_ALREADY_EXISTS.getMessage()));
        }

        @Test
        void createCurrency_currencyDoesNotExistOnCbr_isFailed() throws Exception {
            CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                    .code("MNT")
                    .symbol("₮")
                    .build();

            Assertions.assertThat(currencyRepository.existsByCode(requestDTO.getCode())).isFalse();
            Assertions.assertThat(currencyRepository.existsBySymbol(requestDTO.getSymbol())).isFalse();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(CURRENCIES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.UNSUPPORTED_CURRENCY.getMessage()));
        }
    }

    @Nested
    @DisplayName("exchangeCurrency() Tests")
    class exchangeCurrencyTests {
        @Test
        void exchangeCurrency_fromRubToUsd_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("RUB").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("USD").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRate = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(targetCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInRub = new BigDecimal(10000L);

            BigDecimal amountInUsd = currencyService.exchangeCurrency(sourceCurrency, amountInRub, targetCurrency);

            Assertions.assertThat(amountInUsd).isEqualByComparingTo(amountInRub.divide(exchangeRate, RoundingMode.HALF_EVEN));
        }

        @Test
        void exchangeCurrency_fromUsdToRub_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("USD").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("RUB").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRate = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(sourceCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInUsd = new BigDecimal(100L);

            BigDecimal amountInRub = currencyService.exchangeCurrency(sourceCurrency, amountInUsd, targetCurrency);

            Assertions.assertThat(amountInRub).isEqualByComparingTo(amountInUsd.multiply(exchangeRate));
        }

        @Test
        void exchangeCurrency_fromRubToJpy_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("RUB").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("JPY").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRate = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(targetCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInRub = new BigDecimal(10000L);
            BigDecimal amountInJpy = currencyService.exchangeCurrency(sourceCurrency, amountInRub, targetCurrency);

            Assertions.assertThat(amountInJpy).isEqualByComparingTo(amountInRub.divide(exchangeRate, RoundingMode.HALF_EVEN));
        }

        @Test
        void exchangeCurrency_fromJpyToRub_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("JPY").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("RUB").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRate = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(sourceCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInJpy = new BigDecimal(10000L);

            BigDecimal amountInRub = currencyService.exchangeCurrency(sourceCurrency, amountInJpy, targetCurrency);

            Assertions.assertThat(amountInRub).isEqualByComparingTo(amountInJpy.multiply(exchangeRate));
        }

        @Test
        void exchangeCurrency_fromRubToRub_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("RUB").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("RUB").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isEqualTo(targetCurrency.getCode());

            BigDecimal amountInRub = new BigDecimal(10000L);

            BigDecimal exchangedAmountInRub = currencyService.exchangeCurrency(sourceCurrency, amountInRub, targetCurrency);

            Assertions.assertThat(amountInRub).isEqualByComparingTo(exchangedAmountInRub);
        }

        @Test
        void exchangeCurrency_fromUsdToUsd_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("USD").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("USD").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isEqualTo(targetCurrency.getCode());

            BigDecimal amountInUsd = new BigDecimal(10000L);

            BigDecimal exchangedAmountInUsd = currencyService.exchangeCurrency(sourceCurrency, amountInUsd, targetCurrency);

            Assertions.assertThat(amountInUsd).isEqualByComparingTo(exchangedAmountInUsd);
        }

        @Test
        void exchangeCurrency_fromEurToUsd_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("EUR").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("USD").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRateToRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(sourceCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal exchangeRateFromRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(targetCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInEur = new BigDecimal(10000L);

            BigDecimal amountInUsd = currencyService.exchangeCurrency(sourceCurrency, amountInEur, targetCurrency);

            BigDecimal expected = amountInEur.multiply(exchangeRateToRub).divide(exchangeRateFromRub, RoundingMode.HALF_EVEN);

            Assertions.assertThat(amountInUsd).isEqualByComparingTo(expected);
        }

        @Test
        void exchangeCurrency_fromJpyToUsd_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("JPY").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("USD").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRateToRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(sourceCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal exchangeRateFromRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(targetCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInJpy = new BigDecimal(10000L);

            BigDecimal amountInUsd = currencyService.exchangeCurrency(sourceCurrency, amountInJpy, targetCurrency);

            BigDecimal expected = amountInJpy.multiply(exchangeRateToRub).divide(exchangeRateFromRub, RoundingMode.HALF_EVEN);

            Assertions.assertThat(amountInUsd).isEqualByComparingTo(expected);
        }

        @Test
        void exchangeCurrency_fromUsdToJpy_isSuccessful() {
            Currency sourceCurrency = currencyRepository.findByCode("USD").orElseThrow();
            Currency targetCurrency = currencyRepository.findByCode("JPY").orElseThrow();

            Assertions.assertThat(sourceCurrency.getCode()).isNotEqualTo(targetCurrency.getCode());

            BigDecimal exchangeRateToRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(sourceCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal exchangeRateFromRub = exchangeRateService.findAllLatest().stream()
                    .filter(rate -> rate.getCurrency().getCode().equals(targetCurrency.getCode()))
                    .findFirst().orElseThrow()
                    .getExchangeRate();

            BigDecimal amountInUsd = new BigDecimal(10000L);

            BigDecimal amountInJpy = currencyService.exchangeCurrency(sourceCurrency, amountInUsd, targetCurrency);

            BigDecimal expected = amountInUsd.multiply(exchangeRateToRub).divide(exchangeRateFromRub, RoundingMode.HALF_EVEN);

            Assertions.assertThat(amountInJpy).isEqualByComparingTo(expected);
        }
    }
}
