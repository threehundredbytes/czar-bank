package ru.dreadblade.czarbank.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;
import ru.dreadblade.czarbank.util.MatchersUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Transaction Integration Tests")
@Sql(value = { "/bank-account/bank-accounts-insertion.sql", "/transaction/transactions-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/transaction/transactions-deletion.sql", "/bank-account/bank-accounts-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
    }

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccess() throws Exception {
            long expectedSize = transactionRepository.count();

            Transaction expectedTransaction1 = transactionRepository.findById(BASE_TRANSACTION_ID + 1L).orElseThrow();
            Transaction expectedTransaction4 = transactionRepository.findById(BASE_TRANSACTION_ID + 4L).orElseThrow();

            mockMvc.perform(get("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(jsonPath("$[0].amount").value(MatchersUtils.closeTo(expectedTransaction1.getAmount()), BigDecimal.class))
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").value(expectedTransaction1.getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$[3].amount").value(MatchersUtils.closeTo(expectedTransaction4.getAmount()), BigDecimal.class))
                    .andExpect(jsonPath("$[3].destinationBankAccount.number").value(expectedTransaction4.getDestinationBankAccount().getNumber()));
        }

        @Test
        @Transactional
        void findAll_isEmpty() throws Exception {
            transactionRepository.deleteAll();

            mockMvc.perform(get("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)))
                    .andExpect(jsonPath("$[0].amount").doesNotExist())
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").doesNotExist());
        }
    }

    @Nested
    @DisplayName("findAllByBankAccountId() Tests")
    class findAllByBankAccountIdTests {
        @Test
        void findAllByBankAccountId_isSuccess() throws Exception {
            BankAccount bankAccountForTest = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();

            List<Transaction> expectedTransactions = transactionRepository.findAllByBankAccountId(bankAccountForTest.getId());

            int expectedSize = expectedTransactions.size();

            Assertions.assertThat(expectedSize).isEqualTo(2);

            Transaction expectedTransaction1 = expectedTransactions.get(0);
            Transaction expectedTransaction2 = expectedTransactions.get(1);

            mockMvc.perform(get("/api/bank-accounts/" + bankAccountForTest.getId() + "/transactions")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)))
                    .andExpect(jsonPath("$[0].id").value(expectedTransaction1.getId()))
                    .andExpect(jsonPath("$[0].amount").value(MatchersUtils.closeTo(expectedTransaction1.getAmount()), BigDecimal.class))
                    .andExpect(jsonPath("$[0].sourceBankAccount.number").value(expectedTransaction1.getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$[0].destinationBankAccount.number").value(expectedTransaction1.getDestinationBankAccount().getNumber()))
                    .andExpect(jsonPath("$[1].id").value(expectedTransaction2.getId()))
                    .andExpect(jsonPath("$[1].amount").value(MatchersUtils.closeTo(expectedTransaction2.getAmount()), BigDecimal.class))
                    .andExpect(jsonPath("$[1].sourceBankAccount.number").value(expectedTransaction2.getSourceBankAccount().getNumber()))
                    .andExpect(jsonPath("$[1].destinationBankAccount.number").value(expectedTransaction2.getDestinationBankAccount().getNumber()));
        }

        @Test
        void findAllByBankAccountId_isNotFound() throws Exception {
            long expectedId = BASE_TRANSACTION_ID - 1L;

            mockMvc.perform(get("/api/transactions/" + expectedId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("createTransaction() Tests")
    class createTransactionTests {

        @Test
        @Transactional
        void createTransaction_isSuccess() throws Exception {
            BankAccount sourceBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();
            BankAccount destinationBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow();

            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(sourceBankAccount.getNumber())
                    .destinationBankAccountNumber(destinationBankAccount.getNumber())
                    .build();

            BigDecimal sourceBankAccountBalanceBeforeTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceBeforeTransaction = destinationBankAccount.getBalance();

            long expectedId = BASE_TRANSACTION_ID + transactionRepository.count() + 1;

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(expectedId))
                    .andExpect(jsonPath("$.amount").value(transactionRequest.getAmount()))
                    .andExpect(jsonPath("$.sourceBankAccount.number")
                            .value(transactionRequest.getSourceBankAccountNumber()))
                    .andExpect(jsonPath("$.destinationBankAccount.number")
                            .value(transactionRequest.getDestinationBankAccountNumber()));

            BigDecimal sourceBankAccountBalanceAfterTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceAfterTransaction = destinationBankAccount.getBalance();

            Assertions.assertThat(sourceBankAccountBalanceBeforeTransaction)
                    .isGreaterThan(sourceBankAccountBalanceAfterTransaction);

            Assertions.assertThat(destinationBankAccountBalanceBeforeTransaction)
                    .isLessThan(destinationBankAccountBalanceAfterTransaction);

            Assertions.assertThat(sourceBankAccountBalanceAfterTransaction)
                    .isLessThan(destinationBankAccountBalanceAfterTransaction);

            Assertions.assertThat(sourceBankAccountBalanceAfterTransaction)
                    .isEqualTo(sourceBankAccountBalanceBeforeTransaction.subtract(transactionRequest.getAmount()));

            Assertions.assertThat(destinationBankAccountBalanceAfterTransaction)
                    .isEqualTo(destinationBankAccountBalanceBeforeTransaction.add(transactionRequest.getAmount()));
        }

        @Test
        void createTransaction_sourceBankAccountIsNotFound() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber("11111111111111111111")
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Source bank account doesn't exist"));

        }

        @Test
        void createTransaction_destinationBankAccountIsNotFound() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow().getNumber())
                    .destinationBankAccountNumber("123")
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Destination bank account doesn't exist"));
        }

        @Test
        void createTransaction_sourceBankAccountDoesntHaveEnoughBalanceToCompleteTransaction() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow().getNumber())
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 4L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}
