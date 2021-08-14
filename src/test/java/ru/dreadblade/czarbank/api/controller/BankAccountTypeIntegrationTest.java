package ru.dreadblade.czarbank.api.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("BankAccountType Integration Tests")
@Disabled
public class BankAccountTypeIntegrationTest extends BaseIntegrationTest {
    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccess() {
            
        }

        @Test
        void findAll_isEmpty() {
            
        }
    }
    
    @Nested
    @DisplayName("createBankAccountType() Tests")
    class createBankAccountTypeTests {
        @Test
        void createBankAccountType_isSuccess() {
            
        }

        @Test
        void createBankAccountType_bankAccountTypeWithThisNameAlreadyExists() {
        }
    }

    @Nested
    @DisplayName("updateBankAccountType() Tests")
    class updateBankAccountTypeTests {
        @Test
        void updateBankAccountType_isSuccess() {
            
        }

        @Test
        void updateBankAccountType_isNotFound() {
        }
    }

    @Nested
    @DisplayName("deleteBankAccountType() Tests")
    class deleteBankAccountType {
        @Test
        void deleteBankAccountType_isSuccessful() {

        }

        @Test
        void deleteBankAccountType_isFailed_bankAccountTypeIsUsed() {

        }

        @Test
        void deleteBankAccountType_isNotFound() {

        }
    }
}
