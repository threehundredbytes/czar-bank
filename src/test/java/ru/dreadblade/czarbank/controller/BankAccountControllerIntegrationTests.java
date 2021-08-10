package ru.dreadblade.czarbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.service.BankAccountService;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankAccountController.class)
public class BankAccountControllerIntegrationTests {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    BankAccountService bankAccountService;

    @MockBean
    BankAccountRepository bankAccountRepository;

    @Autowired
    ObjectMapper objectMapper;

    BankAccount MOCK_RECORD_1 = BankAccount.builder()
            .id(1L)
            .number(RandomStringUtils.randomNumeric(20))
            .owner("Owner #1")
            .build();

    BankAccount MOCK_RECORD_2 = BankAccount.builder()
            .id(2L)
            .number(RandomStringUtils.randomNumeric(20))
            .owner("Owner #2")
            .build();

    BankAccount MOCK_RECORD_3 = BankAccount.builder()
            .id(3L)
            .number(RandomStringUtils.randomNumeric(20))
            .owner("Owner #3")
            .build();

    List<BankAccount> records = List.of(MOCK_RECORD_1, MOCK_RECORD_2, MOCK_RECORD_3);


    @Nested
    @DisplayName("getAll() Tests")
    class getAllTests {
        @Test
        void getAll_isSuccess() throws Exception {
            Mockito.when(bankAccountService.getAll()).thenReturn(records);

            mockMvc.perform(get("/api/bank-accounts")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].owner", is(records.get(0).getOwner())))
                    .andExpect(jsonPath("$[1].owner", is(records.get(1).getOwner())))
                    .andExpect(jsonPath("$[2].owner", is(records.get(2).getOwner())));
        }

        @Test
        void getAll_isEmpty() throws Exception {
            Mockito.when(bankAccountService.getAll()).thenReturn(Collections.emptyList());

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
            Mockito.when(bankAccountService.findById(MOCK_RECORD_1.getId())).thenReturn(MOCK_RECORD_1);

            mockMvc.perform(get("/api/bank-accounts/" + MOCK_RECORD_1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.owner", is(MOCK_RECORD_1.getOwner())));
        }

        @Test
        void findById_isNotFound() throws Exception {
            Mockito.when(bankAccountService.findById(MOCK_RECORD_1.getId())).thenThrow(BankAccountNotFoundException.class);

            mockMvc.perform(get("/api/bank-accounts/" + MOCK_RECORD_1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void createAccount_isSuccess() throws Exception {
        Mockito.when(bankAccountService.create(MOCK_RECORD_1.getOwner())).thenReturn(MOCK_RECORD_1);

        mockMvc.perform(post("/api/bank-accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(MOCK_RECORD_1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.owner", is(MOCK_RECORD_1.getOwner())));
    }

    @Test
    void deleteAccount_isSuccess() throws Exception {
        mockMvc.perform(delete("/api/bank-accounts/" + MOCK_RECORD_1.getId()))
                .andExpect(status().isNoContent());
    }
}
