package ru.dreadblade.czarbank.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.exception.BankAccountNotFoundException;
import ru.dreadblade.czarbank.repository.BankAccountRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;


@ExtendWith(MockitoExtension.class)
public class BankAccountServiceUnitTests {
    @Mock
    BankAccountRepository bankAccountRepository;

    @InjectMocks
    BankAccountService bankAccountService;

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
        void getAll_isSuccess() {
            Mockito.when(bankAccountRepository.findAll()).thenReturn(records);

            List<BankAccount> accountsFromDb = bankAccountService.getAll();

            assertThat(accountsFromDb).isEqualTo(accountsFromDb);
        }

        @Test
        void getAll_isEmpty() {
            Mockito.when(bankAccountRepository.findAll()).thenReturn(Collections.emptyList());

            List<BankAccount> accountsFromDb = bankAccountService.getAll();

            assertThat(accountsFromDb).hasSize(0);
            assertThat(accountsFromDb).isEqualTo(Collections.emptyList());
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class findByIdTests {
        @Test
        void findById_isSuccess() {
            Mockito.when(bankAccountRepository.findById(MOCK_RECORD_1.getId())).thenReturn(Optional.of(MOCK_RECORD_1));

            BankAccount accountFromDb = bankAccountService.findById(MOCK_RECORD_1.getId());

            assertThat(accountFromDb.getId()).isEqualTo(MOCK_RECORD_1.getId());
            assertThat(accountFromDb.getOwner()).isEqualTo(MOCK_RECORD_1.getOwner());
            assertThat(accountFromDb.getNumber()).hasSize(MOCK_RECORD_1.getNumber().length());
        }

        @Test
        void findById_isNotFound() throws BankAccountNotFoundException {
            Mockito.when(bankAccountRepository.findById(MOCK_RECORD_1.getId())).thenReturn(Optional.empty());

            Assertions.assertThrows(BankAccountNotFoundException.class,
                    () -> bankAccountService.findById(MOCK_RECORD_1.getId()));
        }
    }


    @Test
    void create_isSuccess() {
        Mockito.when(bankAccountRepository.save(any(BankAccount.class))).thenReturn(MOCK_RECORD_1);

        BankAccount createdAccount = bankAccountService.create(MOCK_RECORD_1.getOwner());

        assertThat(createdAccount.getId()).isEqualTo(MOCK_RECORD_1.getId());
        assertThat(createdAccount.getOwner()).isEqualTo(MOCK_RECORD_1.getOwner());
        assertThat(createdAccount.getNumber()).hasSize(MOCK_RECORD_1.getNumber().length());
    }

    @Nested
    @DisplayName("deleteById() Tests")
    class deleteByIdTests {
        @Test
        void deleteById_isSuccess() {
            Mockito.when(bankAccountRepository.existsById(MOCK_RECORD_1.getId())).thenReturn(true);

            bankAccountService.deleteById(MOCK_RECORD_1.getId());

            Mockito.verify(bankAccountRepository, Mockito.times(1)).deleteById(MOCK_RECORD_1.getId());
        }

        @Test
        void deleteById_isNotFound() {
            Mockito.when(bankAccountRepository.existsById(MOCK_RECORD_1.getId())).thenReturn(false);

            Assertions.assertThrows(BankAccountNotFoundException.class,
                    () -> bankAccountService.deleteById(MOCK_RECORD_1.getId()));

            Mockito.verify(bankAccountRepository, Mockito.times(0)).deleteById(MOCK_RECORD_1.getId());
        }
    }
}