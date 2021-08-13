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
import ru.dreadblade.czarbank.repository.BankAccountRepository;

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
    ObjectMapper objectMapper;

    @Autowired
    BankAccountRepository bankAccountRepository;

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
            long expectedBankAccountId = 4L;
            BigDecimal expectedBalance = new BigDecimal("500.0");
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
    @Transactional
    void deleteAccount_isSuccess() throws Exception {
        long bankAccountDeletionId = 1L;

        mockMvc.perform(delete("/api/bank-accounts/" + bankAccountDeletionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bank-accounts/" + bankAccountDeletionId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
