package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import ru.dreadblade.czarbank.api.mapper.BankAccountTypeMapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountTypeRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountTypeResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.BankAccountTypeRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("BankAccountType Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BankAccountTypeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    BankAccountTypeRepository bankAccountTypeRepository;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    BankAccountTypeMapper bankAccountTypeMapper;

    private static final String BANK_ACCOUNT_TYPES_API_URL = "/api/bank-account-types";

    private static final String VALIDATION_ERROR = "Validation error";
    private static final String INVALID_REQUEST = "Invalid request";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {
        @Test
        void findAll_withoutAuth_isSuccessful() throws Exception {
            List<BankAccountTypeResponseDTO> expectedTypes = bankAccountTypeRepository.findAll().stream()
                    .map(bankAccountTypeMapper::entityToResponseDto)
                    .collect(Collectors.toList());

            long expectedSize = bankAccountTypeRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(expectedTypes);

            mockMvc.perform(get(BANK_ACCOUNT_TYPES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Rollback
        void findAll_withoutAuth_isEmpty() throws Exception {
            bankAccountRepository.deleteAll();
            bankAccountTypeRepository.deleteAll();

            int expectedSize = 0;

            mockMvc.perform(get(BANK_ACCOUNT_TYPES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)));
        }
    }
    
    @Nested
    @DisplayName("createBankAccountType() Tests")
    class CreateBankAccountTypeTests {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void createBankAccountType_withAuth_withPermission_isSuccessful() throws Exception {
            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("New BankAccountType")
                    .transactionCommission(new BigDecimal("0.001"))
                    .currencyExchangeCommission(new BigDecimal("0.001"))
                    .build();

            String expectedResponse = objectMapper.writeValueAsString(bankAccountTypeRequest);

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(content().json(expectedResponse));

            BankAccountType createdType = bankAccountTypeRepository.findByName(bankAccountTypeRequest.getName())
                    .orElseThrow();

            Assertions.assertThat(createdType.getTransactionCommission())
                    .isEqualByComparingTo(bankAccountTypeRequest.getTransactionCommission());
            Assertions.assertThat(createdType.getCurrencyExchangeCommission())
                    .isEqualByComparingTo(bankAccountTypeRequest.getCurrencyExchangeCommission());
        }

        @Test
        @WithUserDetails("client")
        void createBankAccountType_withAuth_isFailed() throws Exception {
            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("New BankAccountType")
                    .transactionCommission(new BigDecimal("0.001"))
                    .currencyExchangeCommission(new BigDecimal("0.001"))
                    .build();

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsByName(bankAccountTypeRequest.getName())).isFalse();
        }

        @Test
        void createBankAccountType_withoutAuth_isFailed() throws Exception {
            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("New BankAccountType")
                    .transactionCommission(new BigDecimal("0.001"))
                    .currencyExchangeCommission(new BigDecimal("0.001"))
                    .build();

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsByName(bankAccountTypeRequest.getName())).isFalse();
        }

        @Test
        @WithUserDetails("admin")
        void createBankAccountType_withAuth_withPermission_bankAccountTypeWithThisNameAlreadyExists() throws Exception {
            BankAccountType typeFromDb = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 3L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name(typeFromDb.getName())
                    .transactionCommission(new BigDecimal("0.001"))
                    .currencyExchangeCommission(new BigDecimal("0.001"))
                    .build();

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS.getMessage()));
        }

        @DisplayName("Validation Tests")
        @Nested
        class ValidationTests {
            @Test
            @WithUserDetails("admin")
            void createBankAccountType_withAuth_withPermission_withBlankName_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name(" ")
                        .transactionCommission(new BigDecimal("0.001"))
                        .currencyExchangeCommission(new BigDecimal("0.001"))
                        .build();

                mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("name"))
                        .andExpect(jsonPath("$.errors[0].message").value("Bank account type name must be not empty"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(BANK_ACCOUNT_TYPES_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createBankAccountType_withAuth_withPermission_withLongName_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name(RandomStringUtils.randomAlphabetic(33))
                        .transactionCommission(new BigDecimal("0.001"))
                        .currencyExchangeCommission(new BigDecimal("0.001"))
                        .build();

                mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("name"))
                        .andExpect(jsonPath("$.errors[0].message").value("The maximum length of the bank account type name is 32 characters"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(BANK_ACCOUNT_TYPES_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createBankAccountType_withAuth_withPermission_withInvalidTransactionCommissionAndCurrencyExchangeCommission_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name("New bank account type")
                        .transactionCommission(new BigDecimal("0.00000000001"))
                        .currencyExchangeCommission(new BigDecimal("1"))
                        .build();

                mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder("transactionCommission", "currencyExchangeCommission")))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder("Transaction commission must contain 0 integers and up to 10 fractional numbers (inclusive)",
                                        "Currency exchange commission must contain 0 integers and up to 10 fractional numbers (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(BANK_ACCOUNT_TYPES_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createBankAccountType_withAuth_withPermission_withNullName_withNullCommissions_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name(null)
                        .transactionCommission(null)
                        .currencyExchangeCommission(null)
                        .build();

                mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(3)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder("name", "transactionCommission", "currencyExchangeCommission")))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder("Bank account type name must be not empty",
                                        "Transaction commission must be not null", "Currency exchange commission must be not null")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(BANK_ACCOUNT_TYPES_API_URL));
            }
        }
    }

    @Nested
    @DisplayName("updateBankAccountType() Tests")
    class UpdateBankAccountTypeTests {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void updateBankAccountType_withAuth_withPermission_isSuccessful() throws Exception {
            BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("Updated unused bank account")
                    .transactionCommission(new BigDecimal("0.07"))
                    .currencyExchangeCommission(new BigDecimal("0.05"))
                    .build();

            String expectedResponse = objectMapper.writeValueAsString(bankAccountTypeRequest);

            mockMvc.perform(put(BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));

            BankAccountType createdType = bankAccountTypeRepository.findByName(bankAccountTypeRequest.getName())
                    .orElseThrow();

            Assertions.assertThat(createdType.getId()).isEqualTo(unusedBankAccountType.getId());
            Assertions.assertThat(createdType.getName()).isEqualTo(bankAccountTypeRequest.getName());

            Assertions.assertThat(createdType.getTransactionCommission())
                    .isEqualByComparingTo(bankAccountTypeRequest.getTransactionCommission());

            Assertions.assertThat(createdType.getCurrencyExchangeCommission())
                    .isEqualByComparingTo(bankAccountTypeRequest.getCurrencyExchangeCommission());
        }

        @Test
        @WithUserDetails("client")
        void updateBankAccountType_withAuth_isFailed() throws Exception {
            BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("Updated unused bank account")
                    .transactionCommission(new BigDecimal("0.07"))
                    .currencyExchangeCommission(new BigDecimal("0.07"))
                    .build();

            mockMvc.perform(put(BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsByName(unusedBankAccountType.getName())).isTrue();
            Assertions.assertThat(bankAccountTypeRepository.existsByName(bankAccountTypeRequest.getName())).isFalse();
        }

        @Test
        void updateBankAccountType_withoutAuth_isFailed() throws Exception {
            BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("Updated unused bank account")
                    .transactionCommission(new BigDecimal("0.07"))
                    .currencyExchangeCommission(new BigDecimal("0.07"))
                    .build();

            mockMvc.perform(put(BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsByName(unusedBankAccountType.getName())).isTrue();
            Assertions.assertThat(bankAccountTypeRepository.existsByName(bankAccountTypeRequest.getName())).isFalse();
        }

        @Test
        @WithUserDetails("admin")
        void updateBankAccountType_withAuth_withPermission_bankAccountTypeWithThisNameAlreadyExists() throws Exception {
            BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                    .orElseThrow();

            BankAccountType junkerBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 3L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name(junkerBankAccountType.getName())
                    .transactionCommission(new BigDecimal("0.07"))
                    .build();

            mockMvc.perform(put(BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS.getMessage()));
        }

        @Test
        @WithUserDetails("admin")
        void updateBankAccountType_withAuth_withPermission_isNotFound() throws Exception {
            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("Updating type that doesn't exist")
                    .transactionCommission(new BigDecimal("0.07"))
                    .build();

            mockMvc.perform(put(BANK_ACCOUNT_TYPES_API_URL + "/" + BASE_BANK_ACCOUNT_TYPE_ID + 123L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Bank account type doesn't exist"));
        }

        @DisplayName("Validation Test")
        @Nested
        class ValidationTest {
            @Test
            @WithUserDetails("admin")
            @Rollback
            void updateBankAccountType_withAuth_withPermission_withLongName_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                        .orElseThrow();

                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name("New BankAccountType (old name is \"" + unusedBankAccountType.getName() + "\")")
                        .transactionCommission(new BigDecimal("0.07"))
                        .currencyExchangeCommission(new BigDecimal("0.05"))
                        .build();

                String requestContent = objectMapper.writeValueAsString(bankAccountTypeRequest);

                String requestUrl = BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("name"))
                        .andExpect(jsonPath("$.errors[0].message").value("The maximum length of the bank account type name is 32 characters"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }

            @Test
            @WithUserDetails("admin")
            @Rollback
            void updateBankAccountType_withAuth_withPermission_withInvalidTransactionCommissionAndCurrencyExchangeCommission_validationIsFailed_responseIsCorrect() throws Exception {
                BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                        .orElseThrow();

                BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                        .name("Updated unused bank account")
                        .transactionCommission(new BigDecimal("1"))
                        .currencyExchangeCommission(new BigDecimal("1"))
                        .build();

                String requestContent = objectMapper.writeValueAsString(bankAccountTypeRequest);

                String requestUrl = BANK_ACCOUNT_TYPES_API_URL + "/" + unusedBankAccountType.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder("transactionCommission", "currencyExchangeCommission")))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder("Transaction commission must contain 0 integers and up to 10 fractional numbers (inclusive)",
                                        "Currency exchange commission must contain 0 integers and up to 10 fractional numbers (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }
        }
    }

    @Nested
    @DisplayName("deleteBankAccountType() Tests")
    class DeleteBankAccountType {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void deleteBankAccountType_withAuth_withPermission_isSuccessful() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 5L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isFalse();
        }

        @Test
        @WithUserDetails("client")
        @Rollback
        void deleteBankAccountType_withAuth_isFailed() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 5L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isTrue();
        }

        @Test
        @Rollback
        void deleteBankAccountType_withoutAuth_isFailed() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 5L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isTrue();
        }


        @Test
        @WithUserDetails("admin")
        void deleteBankAccountType_withAuth_withPermission_isFailed_bankAccountTypeIsUsed() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_IN_USE.getMessage()));

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isTrue();
        }

        @Test
        @WithUserDetails("admin")
        void deleteBankAccountType_withAuth_withPermission_isNotFound() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 123L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_NOT_FOUND.getMessage()));
        }
    }
}
