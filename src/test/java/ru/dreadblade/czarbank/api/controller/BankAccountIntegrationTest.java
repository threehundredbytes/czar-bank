package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import ru.dreadblade.czarbank.api.mapper.BankAccountMapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.BankAccountRepository;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("BankAccount Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class BankAccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    BankAccountMapper bankAccountMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    private static final String BANK_ACCOUNTS_API_URL = "/api/bank-accounts";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {
        @Test
        @WithUserDetails("admin")
        void findAll_withAuth_withPermission_isSuccessful() throws Exception {
            Set<BankAccountResponseDTO> expectedBankAccounts = bankAccountRepository.findAll().stream()
                    .map(bankAccountMapper::bankAccountToBankAccountResponse)
                    .collect(Collectors.toSet());

            long expectedSize = bankAccountRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(expectedBankAccounts);

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findAll_withAuth_isSuccess() throws Exception {
            Long ownerId = BASE_USER_ID + 3L;

            Set<BankAccountResponseDTO> expectedBankAccounts = bankAccountRepository.findAll().stream()
                    .map(bankAccountMapper::bankAccountToBankAccountResponse)
                    .filter(bankAccount -> ownerId.equals(bankAccount.getOwnerId()))
                    .collect(Collectors.toSet());

            long expectedSize = expectedBankAccounts.size();

            String expectedResponse = objectMapper.writeValueAsString(expectedBankAccounts);

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        void findAll_withoutAuth_isFailed() throws Exception {
            mockMvc.perform(get(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        @Rollback
        void findAll_withAuth_withPermission_isEmpty() throws Exception {
            bankAccountRepository.deleteAll();

            int expectedSize = 0;

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)));
        }
    }

    @Nested
    @DisplayName("findById() Tests")
    class FindByIdTests {
        @Test
        @WithUserDetails("admin")
        void findById_withAuth_withPermission_isSuccessful() throws Exception {
            BankAccount expectedBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 4L).orElseThrow();

            BankAccountResponseDTO expectedResponseDTO = bankAccountMapper.bankAccountToBankAccountResponse(expectedBankAccount);

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTO);

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccount.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findById_withAuth_asOwnerOfBankAccount_isSuccessful() throws Exception {
            BankAccount expectedBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();

            BankAccountResponseDTO expectedResponseDTO = bankAccountMapper.bankAccountToBankAccountResponse(expectedBankAccount);

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTO);

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccount.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findById_withAuth_notAsOwner_isFailed() throws Exception {
            BankAccount expectedBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccount.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findById_withoutAuth_isFailed() throws Exception {
            BankAccount expectedBankAccount = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccount.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void findById_withAuth_withPermission_isNotFound() throws Exception {
            long expectedBankAccountId = 123L;

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccountId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        void findById_withAuth_nonExistentAccount_isNotFound() throws Exception {
            long expectedBankAccountId = 123L;

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccountId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        void findById_withoutAuth_nonExistentAccount_isForbidden() throws Exception {
            long expectedBankAccountId = 123L;

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + expectedBankAccountId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }
    }

    @Nested
    @DisplayName("createAccount() Tests")
    class CreateAccountTests {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void createAccount_withAuth_withPermission_isSuccessful() throws Exception {
            Long expectedBankAccountTypeId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;
            Long expectedCurrencyId = BASE_CURRENCY_ID + 1L;

            User userForTest = userRepository.findByUsername("admin").orElseThrow();

            Assertions.assertThat(userForTest.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .contains("BANK_ACCOUNT_CREATE");

            BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder()
                    .ownerId(userForTest.getId())
                    .bankAccountTypeId(expectedBankAccountTypeId)
                    .usedCurrencyId(expectedCurrencyId)
                    .build();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString(BANK_ACCOUNTS_API_URL)))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.number", hasLength(20)))
                    .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                    .andExpect(jsonPath("$.ownerId").value(userForTest.getId()))
                    .andExpect(jsonPath("$.usedCurrencyId").value(expectedCurrencyId))
                    .andExpect(jsonPath("$.bankAccountTypeId").value(expectedBankAccountTypeId));
        }

        @Test
        @WithUserDetails("client")
        @Rollback
        void createAccount_withAuth_asOwner_isSuccessful() throws Exception {
            Long expectedBankAccountTypeId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;
            Long expectedCurrencyId = BASE_CURRENCY_ID + 1L;

            User userForTest = userRepository.findByUsername("client").orElseThrow();

            Assertions.assertThat(userForTest.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .doesNotContain("BANK_ACCOUNT_CREATE");

            BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder()
                    .ownerId(userForTest.getId())
                    .bankAccountTypeId(expectedBankAccountTypeId)
                    .usedCurrencyId(expectedCurrencyId)
                    .build();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString(BANK_ACCOUNTS_API_URL)))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.number", hasLength(20)))
                    .andExpect(jsonPath("$.balance").value(BigDecimal.ZERO))
                    .andExpect(jsonPath("$.ownerId").value(userForTest.getId()))
                    .andExpect(jsonPath("$.usedCurrencyId").value(expectedCurrencyId))
                    .andExpect(jsonPath("$.bankAccountTypeId").value(expectedBankAccountTypeId));
        }

        @Test
        @WithUserDetails("client")
        void createAccount_withAuth_notAsOwner_isFailed() throws Exception {
            Long expectedBankAccountTypeId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;
            Long expectedCurrencyId = BASE_CURRENCY_ID + 1L;

            User userForTest = userRepository.findByUsername("client").orElseThrow();

            Assertions.assertThat(userForTest.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toSet()))
                    .doesNotContain("BANK_ACCOUNT_CREATE");

            BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder()
                    .ownerId(userForTest.getId() + 1L)
                    .bankAccountTypeId(expectedBankAccountTypeId)
                    .usedCurrencyId(expectedCurrencyId)
                    .build();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }


        @Test
        void createAccount_withoutAuth_isFailed() throws Exception {
            Long expectedBankAccountTypeId = BASE_BANK_ACCOUNT_TYPE_ID + 1L;

            BankAccountRequestDTO requestDTO = BankAccountRequestDTO.builder()
                    .bankAccountTypeId(expectedBankAccountTypeId)
                    .usedCurrencyId(BASE_CURRENCY_ID + 1L)
                    .build();

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(BANK_ACCOUNTS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isForbidden())
                    .andExpect(header().doesNotExist(HttpHeaders.LOCATION))
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }
    }

    @Nested
    @DisplayName("deleteAccount() Tests")
    class DeleteAccountTests {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void deleteAccount_withAuth_withPermission_isSuccessful() throws Exception {
            BankAccount bankAccountToDelete = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            Assertions.assertThat(bankAccountToDelete.getOwner().getId()).isNotEqualTo(currentUser.getId());

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountToDelete.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + bankAccountToDelete.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        @Rollback
        void deleteAccount_withAuth_asOwner_isSuccessful() throws Exception {
            BankAccount bankAccountToDelete = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 3L).orElseThrow();

            User currentUser = userRepository.findByUsername("client").orElseThrow();

            Assertions.assertThat(bankAccountToDelete.getOwner().getId()).isEqualTo(currentUser.getId());

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountToDelete.getId()))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get(BANK_ACCOUNTS_API_URL + "/" + bankAccountToDelete.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        void deleteAccount_withAuth_notAsOwner_isFailed() throws Exception {
            BankAccount bankAccountToDelete = bankAccountRepository.findById(BASE_BANK_ACCOUNT_ID + 1L).orElseThrow();

            User currentUser = userRepository.findByUsername("client").orElseThrow();

            Assertions.assertThat(bankAccountToDelete.getOwner().getId()).isNotEqualTo(currentUser.getId());

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountToDelete.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(bankAccountRepository.existsById(bankAccountToDelete.getId())).isTrue();
        }

        @Test
        void deleteAccount_withoutAuth_isFailed() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID + 1L;

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountDeletionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void deleteAccount_withAuth_withPermission_isNotFound() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID - 1L;

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountDeletionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        @WithUserDetails("client")
        void deleteAccount_withAuth_nonExistentAccount_isNotFound() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID - 1L;

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountDeletionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.BANK_ACCOUNT_NOT_FOUND.getMessage()));
        }

        @Test
        void deleteAccount_withoutAuth_nonExistentAccount_isForbidden() throws Exception {
            long bankAccountDeletionId = BASE_BANK_ACCOUNT_ID - 1L;

            mockMvc.perform(delete(BANK_ACCOUNTS_API_URL + "/" + bankAccountDeletionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }
    }
}
