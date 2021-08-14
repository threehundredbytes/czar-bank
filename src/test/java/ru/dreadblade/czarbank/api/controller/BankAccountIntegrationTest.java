package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.BankAccountRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.util.MatchersUtils;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("BankAccount Integration Tests")
@Sql(value = "/bank-account/bank-accounts-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/bank-account/bank-accounts-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BankAccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Nested
    @DisplayName("getAll() Tests")
    class getAllTests {
        @Test
        void getAll_isSuccess() throws Exception {
            BankAccount expectedBankAccount1 = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();
            BankAccount expectedBankAccount3 = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();
            BankAccount expectedBankAccount5 = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 5L).orElseThrow();

            mockMvc.perform(get("/api/bank-accounts")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].owner").value(expectedBankAccount1.getOwner()))
                    .andExpect(jsonPath("$[0].number").value(expectedBankAccount1.getNumber()))
                    .andExpect(jsonPath("$[2].owner").value(expectedBankAccount3.getOwner()))
                    .andExpect(jsonPath("$[2].number").value(expectedBankAccount3.getNumber()))
                    .andExpect(jsonPath("$[4].owner").value(expectedBankAccount5.getOwner()))
                    .andExpect(jsonPath("$[4].number").value(expectedBankAccount5.getNumber()));
        }

        @Test
        @Transactional
        void getAll_isEmpty() throws Exception {
            bankAccountRepository.deleteAll();

            mockMvc.perform(get("/api/bank-accounts")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class findById {
        @Test
        void findById_isSuccess() throws Exception {
            BankAccount expectedBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 4L).orElseThrow();

            mockMvc.perform(get("/api/bank-accounts/" + expectedBankAccount.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id")
                            .value(expectedBankAccount.getId()))
                    .andExpect(jsonPath("$.balance")
                            .value(MatchersUtils.closeTo(expectedBankAccount.getBalance()), BigDecimal.class))
                    .andExpect(jsonPath("$.number")
                            .value(expectedBankAccount.getNumber()))
                    .andExpect(jsonPath("$.owner")
                            .value(expectedBankAccount.getOwner()));
        }

        @Test
        void findById_isNotFound() throws Exception {
            long expectedBankAccountId = 123L;

            mockMvc.perform(get("/api/bank-accounts/" + expectedBankAccountId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void createAccount_isSuccess() throws Exception {
        BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder().owner("Owner #6").build();

        mockMvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner").value(requestDTO.getOwner()))
                .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                .andExpect(jsonPath("$.number", hasLength(20)));
    }

    @Nested
    @DisplayName("deleteAccount() Tests")
    class deleteAccount {
        @Test
        @Transactional
        void deleteAccount_isSuccess() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID + 1L;

            mockMvc.perform(delete("/api/bank-accounts/" + bankAccountDeletionId))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/bank-accounts/" + bankAccountDeletionId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        void deleteAccount_isNotFound() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID - 1L;

            mockMvc.perform(delete("/api/bank-accounts/" + bankAccountDeletionId))
                    .andExpect(status().isNotFound());
        }
    }
}
