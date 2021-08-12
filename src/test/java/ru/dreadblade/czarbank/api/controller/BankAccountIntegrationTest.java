package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import ru.dreadblade.czarbank.api.model.request.BankAccountRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.repository.BankAccountRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("BankAccount Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BankAccountIntegrationTest extends BaseIntegrationTest {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        insertBankAccounts();
    }

    @Nested
    @DisplayName("getAll() Tests")
    class getAllTests {
        @Test
        void getAll_isSuccess() throws Exception {
            String expectedOwner1 = "Owner #1";
            String expectedNumber1 = "39903336089073190794";
            String expectedOwner3 = "Owner #3";
            String expectedNumber3 = "38040432731497506063";
            String expectedOwner5 = "Owner #5";
            String expectedNumber5 = "32541935657215432384";

            mockMvc.perform(get("/api/bank-accounts")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0].owner").value(expectedOwner1))
                    .andExpect(jsonPath("$[0].number").value(expectedNumber1))
                    .andExpect(jsonPath("$[2].owner").value(expectedOwner3))
                    .andExpect(jsonPath("$[2].number").value(expectedNumber3))
                    .andExpect(jsonPath("$[4].owner").value(expectedOwner5))
                    .andExpect(jsonPath("$[4].number").value(expectedNumber5));
        }

        @Test
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
            long expectedBankAccountId = 4L;
            BigDecimal expectedBalance = BigDecimal.valueOf(500.0);
            String expectedNumber = "36264421013439107929";
            String expectedOwner = "Owner #4";


            mockMvc.perform(get("/api/bank-accounts/" + expectedBankAccountId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(expectedBankAccountId))
                    .andExpect(jsonPath("$.balance").value(expectedBalance))
                    .andExpect(jsonPath("$.number").value(expectedNumber))
                    .andExpect(jsonPath("$.owner").value(expectedOwner));
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

    @Test
    void deleteAccount_isSuccess() throws Exception {
        long bankAccountDeletionId = 1L;

        mockMvc.perform(delete("/api/bank-accounts/" + bankAccountDeletionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bank-accounts/" + bankAccountDeletionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private void insertBankAccounts() {
        BankAccount bankAccount1 = BankAccount.builder()
                .balance(BigDecimal.valueOf(15000L))
                .number("39903336089073190794")
                .owner("Owner #1")
                .build();

        BankAccount bankAccount2 = BankAccount.builder()
                .balance(BigDecimal.valueOf(5000L))
                .number("33390474811219980161")
                .owner("Owner #2")
                .build();

        BankAccount bankAccount3 = BankAccount.builder()
                .balance(BigDecimal.valueOf(2000L))
                .number("38040432731497506063")
                .owner("Owner #3")
                .build();

        BankAccount bankAccount4 = BankAccount.builder()
                .balance(BigDecimal.valueOf(500L))
                .number("36264421013439107929")
                .owner("Owner #4")
                .build();

        BankAccount bankAccount5 = BankAccount.builder()
                .balance(BigDecimal.valueOf(1500L))
                .number("32541935657215432384")
                .owner("Owner #5")
                .build();

        bankAccountRepository.saveAll(List.of(
                bankAccount1,
                bankAccount2,
                bankAccount3,
                bankAccount4,
                bankAccount5
        ));
    }
}
