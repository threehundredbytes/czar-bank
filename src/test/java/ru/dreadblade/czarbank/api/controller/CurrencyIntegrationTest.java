package ru.dreadblade.czarbank.api.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
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

import static org.hamcrest.Matchers.*;
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
    class FindAllTests {
        @Test
        void findAll_withoutAuth_isSuccessful() throws Exception {
            long expectedSize = currencyRepository.count();

            List<CurrencyResponseDTO> expectedDTOs = currencyRepository.findAll().stream()
                    .map(currencyMapper::entityToResponseDto)
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
    class CreateCurrencyTests {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void createCurrency_withAuth_withPermission_isSuccessful() throws Exception {
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
        @WithUserDetails("client")
        void createCurrency_withAuth_isFailed() throws Exception {
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
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void createCurrency_withoutAuth_isFailed() throws Exception {
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
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void createCurrency_withAuth_withPermission_currencyCodeAlreadyExists_isFailed() throws Exception {
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
        @WithUserDetails("admin")
        void createCurrency_withAuth_withPermission_currencySymbolAlreadyExists_isFailed() throws Exception {
            CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                    .code("NEW")
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
        @WithUserDetails("admin")
        void createCurrency_withAuth_withPermission_currencyDoesNotExistOnCbr_isFailed() throws Exception {
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

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {
            @Test
            @WithUserDetails("admin")
            void createCurrency_withAuth_withPermission_withNullCodeAndSymbol_validationIsFailed_responseIsCorrect() throws Exception {
                CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                        .code(null)
                        .symbol(null)
                        .build();

                String requestContent = objectMapper.writeValueAsString(requestDTO);

                mockMvc.perform(post(CURRENCIES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder("code", "symbol")))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder("Currency code must be not empty",
                                        "Currency symbol must be not empty")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(CURRENCIES_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createCurrency_withAuth_withPermission_withInvalidCodeAndSymbol_isSuccessful() throws Exception {
                CurrencyRequestDTO requestDTO = CurrencyRequestDTO.builder()
                        .code(RandomStringUtils.randomAlphabetic(4))
                        .symbol(RandomStringUtils.randomAlphabetic(5))
                        .build();

                String requestContent = objectMapper.writeValueAsString(requestDTO);

                mockMvc.perform(post(CURRENCIES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder("code", "symbol")))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder("The length of the currency code must be 3 characters",
                                        "The length of the currency symbol must be between 1 and 4 characters")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(CURRENCIES_API_URL));
            }
        }
    }

    @Nested
    @DisplayName("exchangeCurrency() Tests")
    class ExchangeCurrencyTests {
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
