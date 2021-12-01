package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.mapper.TransactionMapper;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.TransactionResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.BankAccountService;
import ru.dreadblade.czarbank.service.CurrencyService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    CurrencyService currencyService;

    private static final String TRANSACTIONS_API_URL = "/api/transactions";
    private static final String BANK_ACCOUNTS_API_URL = "/api/bank-accounts";
    private static final String TRANSACTIONS = "transactions";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {

        @Test
        @WithUserDetails("admin")
        void findAll_withAuth_withPermission_isSuccessful() throws Exception {
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
        @WithUserDetails("client")
        void findAll_withAuth_isFailed() throws Exception {
            mockMvc.perform(get(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findAll_withoutAuth_isFailed() throws Exception {
            mockMvc.perform(get(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        @Rollback
        void findAll_withAuth_withPermission_isEmpty() throws Exception {
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
    class FindAllByBankAccountIdTests {

        @Test
        @WithUserDetails("admin")
        void findAllByBankAccountId_withAuth_withPermission_isSuccessful() throws Exception {
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
        @WithUserDetails("client")
        void findAllByBankAccountId_withAuth_asOwner_isSuccessful() throws Exception {
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
        @WithUserDetails("client")
        void findAllByBankAccountId_withAuth_notAsOwner_isFailed() throws Exception {
            BankAccount bankAccountForTest = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + bankAccountForTest.getId() + "/" + TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findAllByBankAccountId_withoutAuth_isFailed() throws Exception {
            BankAccount bankAccountForTest = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + bankAccountForTest.getId() + "/" + TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void findAllByBankAccountId_withAuth_withPermission_isNotFound() throws Exception {
            long expectedId = BASE_BANK_ACCOUNT_ID - 1L;

            Assertions.assertThat(bankAccountRepository.existsById(expectedId)).isFalse();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedId + "/" + TRANSACTIONS)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithUserDetails("client")
        void findAllByBankAccountId_withAuth_nonExistentAccount_isNotFound() throws Exception {
            long expectedId = BASE_BANK_ACCOUNT_ID - 1L;

            Assertions.assertThat(bankAccountRepository.existsById(expectedId)).isFalse();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedId + "/" + TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        void findAllByBankAccountId_withoutAuth_nonExistentAccount_isForbidden() throws Exception {
            long expectedId = BASE_BANK_ACCOUNT_ID - 1L;

            Assertions.assertThat(bankAccountRepository.existsById(expectedId)).isFalse();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedId + "/" + TRANSACTIONS)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }
    }

    @Nested
    @DisplayName("createTransaction() Tests")
    class CreateTransactionTests {
        @Test
        @WithUserDetails("admin")
        @Transactional
        void createTransaction_withAuth_withPermission_sameCurrencies_isSuccessful() throws Exception {
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
                    .andExpect(jsonPath("$.receivedAmount").value(transactionRequest.getAmount()))
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
        @WithUserDetails("client")
        @Transactional
        void createTransaction_withAuth_asOwnerOfSourceBankAccount_sameCurrencies_isSuccessful() throws Exception {
            BankAccount sourceBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();
            BankAccount destinationBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow();

            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1000L))
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
                    .andExpect(jsonPath("$.receivedAmount").value(transactionRequest.getAmount()))
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
        @WithUserDetails("client")
        @Transactional
        void createTransaction_withAuth_notAsOwnerOfSourceBankAccount_sameCurrencies_isFailed() throws Exception {
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

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            BigDecimal sourceBankAccountBalanceAfterTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceAfterTransaction = destinationBankAccount.getBalance();

            Assertions.assertThat(sourceBankAccountBalanceBeforeTransaction)
                    .isEqualTo(sourceBankAccountBalanceAfterTransaction);

            Assertions.assertThat(destinationBankAccountBalanceBeforeTransaction)
                    .isEqualTo(destinationBankAccountBalanceAfterTransaction);

            Assertions.assertThat(sourceBankAccountBalanceAfterTransaction)
                    .isEqualTo(sourceBankAccountBalanceBeforeTransaction);

            Assertions.assertThat(destinationBankAccountBalanceAfterTransaction)
                    .isEqualTo(destinationBankAccountBalanceBeforeTransaction);
        }

        @Test
        @Transactional
        void createTransaction_withoutAuth_sameCurrencies_isFailed() throws Exception {
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

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            BigDecimal sourceBankAccountBalanceAfterTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceAfterTransaction = destinationBankAccount.getBalance();

            Assertions.assertThat(sourceBankAccountBalanceBeforeTransaction)
                    .isEqualTo(sourceBankAccountBalanceAfterTransaction);

            Assertions.assertThat(destinationBankAccountBalanceBeforeTransaction)
                    .isEqualTo(destinationBankAccountBalanceAfterTransaction);

            Assertions.assertThat(sourceBankAccountBalanceAfterTransaction)
                    .isEqualTo(sourceBankAccountBalanceBeforeTransaction);

            Assertions.assertThat(destinationBankAccountBalanceAfterTransaction)
                    .isEqualTo(destinationBankAccountBalanceBeforeTransaction);
        }

        @Test
        @WithUserDetails("admin")
        @Transactional
        void createTransaction_withAuth_withPermission_currenciesDiffer_isSuccessful() throws Exception {
            BankAccount sourceBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            long ownerId = BASE_USER_ID + 1L;
            long bankAccountTypeId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;
            long currencyId = BASE_CURRENCY_ID + 2L;

            BankAccount destinationBankAccount = bankAccountService.create(ownerId, bankAccountTypeId, currencyId);

            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber(sourceBankAccount.getNumber())
                    .destinationBankAccountNumber(destinationBankAccount.getNumber())
                    .build();

            BigDecimal sourceBankAccountBalanceBeforeTransaction = sourceBankAccount.getBalance();
            BigDecimal destinationBankAccountBalanceBeforeTransaction = destinationBankAccount.getBalance();

            BigDecimal transactionAmount = transactionRequest.getAmount();

            BankAccountType sourceBankAccountType = sourceBankAccount.getBankAccountType();

            BigDecimal commission = sourceBankAccountType.getTransactionCommission()
                    .add(sourceBankAccountType.getCurrencyExchangeCommission());

            BigDecimal transactionAmountWithCommission = transactionAmount.add(transactionAmount
                    .multiply(commission));

            Assertions.assertThat(sourceBankAccountBalanceBeforeTransaction)
                    .isGreaterThanOrEqualTo(transactionAmountWithCommission);

            BigDecimal expectedReceivedAmount = currencyService.exchangeCurrency(sourceBankAccount.getUsedCurrency(),
                    transactionAmount, destinationBankAccount.getUsedCurrency());

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.amount").value(transactionRequest.getAmount()))
                    .andExpect(jsonPath("$.receivedAmount").value(expectedReceivedAmount))
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
                    .isEqualTo(sourceBankAccountBalanceBeforeTransaction.subtract(transactionAmountWithCommission));

            Assertions.assertThat(destinationBankAccountBalanceAfterTransaction)
                    .isEqualTo(destinationBankAccountBalanceBeforeTransaction.add(expectedReceivedAmount));
        }

        @Test
        @WithUserDetails("admin")
        void createTransaction_withAuth_withPermission_nonExistentSourceBankAccount_isBadRequest() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber("11111111111111111111")
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.SOURCE_BANK_ACCOUNT_DOESNT_EXIST.getMessage()));

        }

        @Test
        @WithUserDetails("client")
        void createTransaction_withAuth_nonExistentSourceBankAccount_isBadRequest() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber("11111111111111111111")
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.SOURCE_BANK_ACCOUNT_DOESNT_EXIST.getMessage()));

        }

        @Test
        void createTransaction_withoutAuth_nonExistentSourceBankAccount_isForbidden() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(10000L))
                    .sourceBankAccountNumber("11111111111111111111")
                    .destinationBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 2L).orElseThrow().getNumber())
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

        }

        @Test
        @WithUserDetails("admin")
        void createTransaction_withAuth_withPermission_nonExistentDestinationBankAccount_isBadRequest() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow().getNumber())
                    .destinationBankAccountNumber("123")
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.DESTINATION_BANK_ACCOUNT_DOESNT_EXIST.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        void createTransaction_withAuth_asOwnerOfSourceBankAccount_nonExistentDestinationBankAccount_isBadRequest() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow().getNumber())
                    .destinationBankAccountNumber("123")
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.DESTINATION_BANK_ACCOUNT_DOESNT_EXIST.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        void createTransaction_withAuth_notAsOwnerOfSourceBankAccount_nonExistentDestinationBankAccount_isForbidden() throws Exception {
            TransactionRequestDTO transactionRequest = TransactionRequestDTO.builder()
                    .amount(BigDecimal.valueOf(1L))
                    .sourceBankAccountNumber(bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow().getNumber())
                    .destinationBankAccountNumber("123")
                    .build();

            mockMvc.perform(post(TRANSACTIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message")
                            .value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void createTransaction_withAuth_withPermission_sourceBankAccountDoesntHaveEnoughBalanceToCompleteTransaction() throws Exception {
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
    }
}
