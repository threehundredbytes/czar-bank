package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
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

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            List<BankAccountTypeResponseDTO> expectedTypes = bankAccountTypeRepository.findAll().stream()
                    .map(bankAccountTypeMapper::bankAccountTypeToBankAccountTypeResponse)
                    .collect(Collectors.toList());

            long expectedSize = bankAccountTypeRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(expectedTypes);

            mockMvc.perform(get(BANK_ACCOUNT_TYPES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Transactional
        void findAll_isEmpty() throws Exception {
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
    class createBankAccountTypeTests {
        @Test
        @Transactional
        void createBankAccountType_isSuccessful() throws Exception {
            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("New BankAccountType")
                    .transactionCommission(new BigDecimal("0.07"))
                    .build();

            String expectedResponse = objectMapper.writeValueAsString(bankAccountTypeRequest);

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().json(expectedResponse));

            BankAccountType createdType = bankAccountTypeRepository.findByName(bankAccountTypeRequest.getName())
                    .orElseThrow();

            Assertions.assertThat(createdType.getName()).isEqualTo(bankAccountTypeRequest.getName());

            Assertions.assertThat(createdType.getTransactionCommission())
                    .isEqualTo(bankAccountTypeRequest.getTransactionCommission());
        }

        @Test
        void createBankAccountType_bankAccountTypeWithThisNameAlreadyExists() throws Exception {
            BankAccountType typeFromDb = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 3L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name(typeFromDb.getName())
                    .transactionCommission(new BigDecimal("0.07"))
                    .build();

            mockMvc.perform(post(BANK_ACCOUNT_TYPES_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bankAccountTypeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_NAME_ALREADY_EXISTS.getMessage()));
        }
    }

    @Nested
    @DisplayName("updateBankAccountType() Tests")
    class updateBankAccountTypeTests {
        @Test
        @Transactional
        void updateBankAccountType_isSuccessful() throws Exception {
            BankAccountType unusedBankAccountType = bankAccountTypeRepository.findById(BASE_BANK_ACCOUNT_TYPE_ID + 5L)
                    .orElseThrow();

            BankAccountTypeRequestDTO bankAccountTypeRequest = BankAccountTypeRequestDTO.builder()
                    .name("New BankAccountType (old name is \"" + unusedBankAccountType.getName() + "\")")
                    .transactionCommission(new BigDecimal("0.07"))
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
                    .isEqualTo(bankAccountTypeRequest.getTransactionCommission());
        }

        @Test
        void updateBankAccountType_bankAccountTypeWithThisNameAlreadyExists() throws Exception {
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
        void updateBankAccountType_isNotFound() throws Exception {
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
    }

    @Nested
    @DisplayName("deleteBankAccountType() Tests")
    class deleteBankAccountType {
        @Test
        @Transactional
        void deleteBankAccountType_isSuccessful() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 5L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isFalse();
        }

        @Test
        void deleteBankAccountType_isFailed_bankAccountTypeIsUsed() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_IN_USE.getMessage()));

            Assertions.assertThat(bankAccountTypeRepository.existsById(bankAccountTypeDeletionId)).isTrue();
        }

        @Test
        void deleteBankAccountType_isNotFound() throws Exception {
            long bankAccountTypeDeletionId = BASE_BANK_ACCOUNT_TYPE_ID + 123L;

            mockMvc.perform(delete(BANK_ACCOUNT_TYPES_API_URL + "/" + bankAccountTypeDeletionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_TYPE_NOT_FOUND.getMessage()));
        }
    }
}
