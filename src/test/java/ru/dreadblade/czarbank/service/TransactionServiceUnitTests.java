package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.exception.NotEnoughBalanceException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUnitTests {
    @Mock
    BankAccountRepository bankAccountRepository;

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionService transactionService;

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

    @Nested
    @DisplayName("findAll() Tests")
    class findAll {
        @Test
        void findAll_isSuccess() {
            Mockito.when(transactionRepository.findAll()).thenReturn(transactions);

            List<Transaction> transactionsFromDb = transactionService.findAll();

            assertThat(transactionsFromDb).isEqualTo(transactions);
        }

        @Test
        void findAll_isEmpty() {
            Mockito.when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

            List<Transaction> transactionsFromDb = transactionService.findAll();

            assertThat(transactionsFromDb).isEqualTo(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("findAllByBankAccountId() Tests")
    class findAllByBankAccountId {
        @Test
        void findAllByBankAccountId_isSuccess() {
            Mockito.when(transactionRepository.findAllByBankAccountId(MOCK_ACCOUNT_1.getId())).thenReturn(transactions);

            Mockito.when(bankAccountRepository.existsById(MOCK_ACCOUNT_1.getId())).thenReturn(true);
            List<Transaction> transactionsFromDb = transactionService.findAllByBankAccountId(MOCK_ACCOUNT_1.getId());

            assertThat(transactionsFromDb).isEqualTo(transactions);
        }

        @Test
        void findAllByBankAccountId_isEmpty() {
            BankAccount MOCK_ACCOUNT_3 = BankAccount.builder()
                    .id(3L)
                    .number(RandomStringUtils.randomNumeric(20))
                    .owner("Owner #3")
                    .balance(BigDecimal.valueOf(3000L))
                    .build();

            Mockito.when(bankAccountRepository.existsById(MOCK_ACCOUNT_3.getId())).thenReturn(true);
            Mockito.when(transactionRepository.findAllByBankAccountId(MOCK_ACCOUNT_3.getId())).thenReturn(Collections.emptyList());

            List<Transaction> transactionsFromDb = transactionService.findAllByBankAccountId(MOCK_ACCOUNT_3.getId());

            assertThat(transactionsFromDb).isEqualTo(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("createTransaction() Tests")
    class createTransaction {
        @Test
        void createTransaction_isSuccess() {
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_1.getId())).thenReturn(Optional.of(MOCK_ACCOUNT_1));
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_2.getId())).thenReturn(Optional.of(MOCK_ACCOUNT_2));

            Transaction transactionAfterSaving = Transaction.builder()
                    .id(1L)
                    .datetime(Instant.now())
                    .amount(BigDecimal.valueOf(1000L))
                    .sourceBankAccount(MOCK_ACCOUNT_1)
                    .destinationBankAccount(MOCK_ACCOUNT_2)
                    .build();

            Mockito.when(transactionRepository.save(MOCK_TRANSACTION_1)).thenReturn(transactionAfterSaving);

            BigDecimal sourceAccountBalanceBeforeTransaction = MOCK_ACCOUNT_1.getBalance().add(BigDecimal.ZERO);
            BigDecimal destAccountBalanceBeforeTransaction = MOCK_ACCOUNT_2.getBalance().add(BigDecimal.ZERO);

            Transaction transaction = transactionService.createTransaction(MOCK_TRANSACTION_1);

            assertThat(transaction).isNotNull();
            assertThat(transaction.getSourceBankAccount()).isNotNull();
            assertThat(transaction.getDestinationBankAccount()).isNotNull();

            BankAccount source = transaction.getSourceBankAccount();
            BankAccount destination = transaction.getDestinationBankAccount();

            assertThat(source.getBalance()).isLessThan(sourceAccountBalanceBeforeTransaction);
            assertThat(destination.getBalance()).isGreaterThan(destAccountBalanceBeforeTransaction);
        }

        @Test
        void createTransaction_sourceBankAccountIsNotFound() {
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_1.getId())).thenReturn(Optional.of(MOCK_ACCOUNT_1));
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_2.getId())).thenReturn(Optional.empty());

            Assertions.assertThrows(BankAccountNotFoundException.class,
                    () -> transactionService.createTransaction(MOCK_TRANSACTION_1));
        }

        @Test
        void createTransaction_destinationBankAccountIsNotFound() {
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_1.getId())).thenReturn(Optional.empty());

            Assertions.assertThrows(BankAccountNotFoundException.class,
                    () -> transactionService.createTransaction(MOCK_TRANSACTION_1));
        }

        @Test
        void createTransaction_sourceBankAccountDoesntHaveEnoughBalanceToCompleteTransaction() {
            Mockito.when(bankAccountRepository.findById(MOCK_ACCOUNT_2.getId())).thenReturn(Optional.of(MOCK_ACCOUNT_2));

            Transaction MOCK_TRANSACTION_NOT_ENOUGH_BALANCE = Transaction.builder()
                    .id(4L)
                    .amount(BigDecimal.valueOf(1000L))
                    .sourceBankAccount(MOCK_ACCOUNT_2)
                    .destinationBankAccount(MOCK_ACCOUNT_1)
                    .build();

            Assertions.assertThrows(NotEnoughBalanceException.class,
                    () -> transactionService.createTransaction(MOCK_TRANSACTION_NOT_ENOUGH_BALANCE));


        }
    }
}
