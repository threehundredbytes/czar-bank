package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;
import ru.dreadblade.czarbank.service.TransactionService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class TransactionControllerIntegrationTests {
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @MockBean
    TransactionService transactionService;

    @MockBean
    TransactionRepository transactionRepository;

    @MockBean
    BankAccountRepository bankAccountRepository;

    @Autowired
    ObjectMapper objectMapper;

    BankAccount MOCK_ACCOUNT_1 = BankAccount.builder()
            .id(1L)
            .number(RandomStringUtils.randomNumeric(20))
            .owner("Owner #1")
            .balance(BigDecimal.valueOf(2000L))
            .build();

    BankAccount MOCK_ACCOUNT_2 = BankAccount.builder()
            .id(2L)
            .number(RandomStringUtils.randomNumeric(20))
            .owner("Owner #2")
            .balance(BigDecimal.valueOf(500L))
            .build();

    Transaction MOCK_TRANSACTION_1 = Transaction.builder()
            .id(1L)
            .amount(BigDecimal.valueOf(1000L))
            .sourceBankAccount(MOCK_ACCOUNT_1)
            .destinationBankAccount(MOCK_ACCOUNT_2)
            .build();

    Transaction MOCK_TRANSACTION_2 = Transaction.builder()
            .id(2L)
            .amount(BigDecimal.valueOf(200L))
            .sourceBankAccount(MOCK_ACCOUNT_1)
            .destinationBankAccount(MOCK_ACCOUNT_2)
            .build();

    Transaction MOCK_TRANSACTION_3 = Transaction.builder()
            .id(3L)
            .amount(BigDecimal.valueOf(350L))
            .sourceBankAccount(MOCK_ACCOUNT_1)
            .destinationBankAccount(MOCK_ACCOUNT_2)
            .build();

    List<Transaction> transactions = List.of(MOCK_TRANSACTION_1, MOCK_TRANSACTION_2, MOCK_TRANSACTION_3);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccess() throws Exception {
            Mockito.when(transactionService.findAll()).thenReturn(transactions);

            mockMvc.perform(get("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].amount").value(transactions.get(0).getAmount()))
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").value(transactions.get(0).getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$[2].amount").value(transactions.get(2).getAmount()))
                    .andExpect(jsonPath("$[2].destinationBankAccount.number").value(transactions.get(2).getDestinationBankAccount().getNumber()));
        }

        @Test
        void findAll_isEmpty() throws Exception {
            Mockito.when(transactionService.findAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$[0].amount").doesNotExist())
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").doesNotExist());
        }
    }

    @Nested
    @DisplayName("findAllByBankId() Tests")
    class findAllByBankAccountIdTests {
        @Test
        void findAllByBankAccountId_isSuccess() throws Exception {
            Mockito.when(transactionService.findAllByBankAccountId(MOCK_ACCOUNT_1.getId())).thenReturn(transactions);

            mockMvc.perform(get("/api/transactions/" + MOCK_ACCOUNT_1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].sourceBankAccount.id").value(MOCK_ACCOUNT_1.getId()))
                    .andExpect(jsonPath("$[0].amount").value(transactions.get(0).getAmount()))
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").value(transactions.get(0).getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$[1].sourceBankAccount.id").value(MOCK_ACCOUNT_1.getId()))
                    .andExpect(jsonPath("$[2].sourceBankAccount.id").value(MOCK_ACCOUNT_1.getId()))
                    .andExpect(jsonPath("$[2].amount").value(transactions.get(2).getAmount()))
                    .andExpect(jsonPath("$[2].destinationBankAccount.number").value(transactions.get(2).getDestinationBankAccount().getNumber()));
        }

        @Test
        void findAllByBankAccountId_isNotFound() throws Exception {
            Long id = MOCK_TRANSACTION_3.getId() + 123;

            Mockito.when(transactionService.findAllByBankAccountId(id))
                    .thenThrow(BankAccountNotFoundException.class);

            mockMvc.perform(get("/api/transactions/" + id)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("createTransaction() Tests")
    @Disabled
    class createTransactionTests {
        @Test
        void createTransaction_isSuccess() throws Exception {
            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MOCK_TRANSACTION_1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.amount").value(1000L))
                    .andExpect(jsonPath("$.sourceBankAccount.number")
                            .value(MOCK_TRANSACTION_1.getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$.destinationBankAccount.number")
                            .value(MOCK_TRANSACTION_1.getDestinationBankAccount().getNumber()));

        }

        @Test
        void createTransaction_sourceBankAccountIsNotFound() throws Exception {
            Mockito.when(bankAccountRepository.existsById(any())).thenReturn(false);

            Mockito.when(transactionService.createTransaction(any(Transaction.class))).thenCallRealMethod();

            Transaction MOCK_TRANSACTION_WITHOUT_SOURCE_BANK_ACCOUNT = Transaction.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(2000L))
                    .sourceBankAccount(BankAccount.builder().number("123").build())
                    .destinationBankAccount(MOCK_ACCOUNT_2)
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MOCK_TRANSACTION_WITHOUT_SOURCE_BANK_ACCOUNT)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createTransaction_destinationBankAccountIsNotFound() throws Exception {
            Transaction MOCK_TRANSACTION_WITHOUT_DESTINATION_BANK_ACCOUNT = Transaction.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(2000L))
                    .sourceBankAccount(MOCK_ACCOUNT_1)
                    .destinationBankAccount(BankAccount.builder().number("123").build())
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MOCK_TRANSACTION_WITHOUT_DESTINATION_BANK_ACCOUNT)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createTransaction_sourceBankAccountDoesntHaveEnoughBalanceToCompleteTransaction() throws Exception {
            Transaction MOCK_TRANSACTION = Transaction.builder()
                    .id(2L)
                    .amount(BigDecimal.valueOf(2000L))
                    .sourceBankAccount(MOCK_ACCOUNT_2)
                    .destinationBankAccount(MOCK_ACCOUNT_1)
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(MOCK_TRANSACTION)))
                    .andExpect(status().isBadRequest());
        }
    }


}
