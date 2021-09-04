package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.mapper.TransactionMapper;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.TransactionResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.BankAccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Transaction Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql", "/transaction/transactions-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/transaction/transactions-deletion.sql", "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class TransactionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    TransactionMapper transactionMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BankAccountService bankAccountService;

    private static final String TRANSACTIONS_API_URL = "/api/transactions";
    private static final String BANK_ACCOUNTS_API_URL = "/api/bank-accounts";
    private static final String TRANSACTIONS = "transactions";

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {

        @Test
        void findAll_isSuccessful() throws Exception {
            List<TransactionResponseDTO> expectedTransactions = transactionRepository.findAll().stream()
                    .map(transactionMapper::transactionToTransactionResponse)
                    .collect(Collectors.toList());

            long expectedSize = transactionRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(expectedTransactions);

            mockMvc.perform(get(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Transactional
        void findAll_isEmpty() throws Exception {
            transactionRepository.deleteAll();

            int expectedSize = 0;

            Assertions.assertThat(transactionRepository.count()).isEqualTo(expectedSize);

            mockMvc.perform(get(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)))
                    .andExpect(jsonPath("$[0]").doesNotExist());
        }
    }

    @Nested
    @DisplayName("findAllByBankAccountId() Tests")
    class findAllByBankAccountIdTests {

        @Test
        void findAllByBankAccountId_isSuccessful() throws Exception {
            BankAccount bankAccountForTest = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();

            List<TransactionResponseDTO> expectedTransactions = transactionRepository
                    .findAllByBankAccountId(bankAccountForTest.getId())
                    .stream()
                    .map(transactionMapper::transactionToTransactionResponse)
                    .collect(Collectors.toList());

            int expectedSize = expectedTransactions.size();

            String expectedResponse = objectMapper.writeValueAsString(expectedTransactions);

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + bankAccountForTest.getId() + "/" + TRANSACTIONS)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        void findAllByBankAccountId_isNotFound() throws Exception {
            long expectedId = BASE_BANK_ACCOUNT_ID - 1L;

            Assertions.assertThat(bankAccountRepository.existsById(expectedId)).isFalse();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedId + "/" + TRANSACTIONS)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("createTransaction() Tests")
    class createTransactionTests {

        @Test
        @Transactional
        void createTransaction_isSuccessful() throws Exception {
            BankAccount sourceBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();
            BankAccount destinationBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow();

            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(sourceBankAccount.getNumber())
                    .destinationBankAccountNumber(destinationBankAccount.getNumber())
                    .build();

            BigDecimal sourceBankAccountBalanceBeforeTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceBeforeTransaction = destinationBankAccount.getBalance();

            BigDecimal transactionAmount = transactionRequest.getAmount();

            BigDecimal transactionAmountWithCommission = transactionAmount.add(transactionAmount
                    .multiply(sourceBankAccount.getBankAccountType().getTransactionCommission()));

            Assertions.assertThat(sourceBankAccountBalanceBeforeTransaction)
                    .isGreaterThanOrEqualTo(transactionAmountWithCommission);

            long expectedId = BASE_TRANSACTION_ID + transactionRepository.count() + 1;

            mockMvc.perform(post(TRANSACTIONS_API_URL)
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
                    .isEqualTo(sourceBankAccountBalanceBeforeTransaction.subtract(transactionAmountWithCommission));

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

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));

        }

        @Test
        void createTransaction_destinationBankAccountIsNotFound() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow().getNumber())
                    .destinationBankAccountNumber("123")
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        void createTransaction_sourceBankAccountDoesntHaveEnoughBalanceToCompleteTransaction() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow().getNumber())
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 4L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.NOT_ENOUGH_BALANCE.getMessage()));
        }

        @Test
        @Rollback
        void createTransaction_currenciesDiffer_isFailed() throws Exception {
            BankAccount sourceBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            BankAccount destinationBankAccount = bankAccountService
                    .create(userRepository.findById(BASE_USER_ID + 1L).orElseThrow(),
                            BASE_BANK_ACCOUNT_TYPE_ID + 1L,
                            BASE_CURRENCY_ID + 2L);

            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(sourceBankAccount.getNumber())
                    .destinationBankAccountNumber(destinationBankAccount.getNumber())
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Currency exchange has not yet been implemented"));
        }
    }
}
