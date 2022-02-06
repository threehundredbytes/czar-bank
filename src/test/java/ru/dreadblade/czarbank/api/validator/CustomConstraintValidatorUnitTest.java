package ru.dreadblade.czarbank.api.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.dreadblade.czarbank.api.validation.validator.EmailConstraintValidator;
import ru.dreadblade.czarbank.api.validation.validator.PasswordConstraintValidator;

@DisplayName("Custom ConstraintValidator Unit Tests")
public class CustomConstraintValidatorUnitTest {
    @Nested
    @DisplayName("Complex password constraint tests")
    class ComplexPasswordConstraintTests {
        @Test
        void validatePassword_nullPasswordConsideredValid_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid(null, null)).isTrue();
        }

        @Test
        void validatePassword_blankPassword_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid(" ", null)).isFalse();
        }

        @Test
        void validatePassword_shortPassword_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid("pass", null)).isFalse();
        }

        @Test
        void validatePassword_lowercaseLetters_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid("easypassword", null)).isFalse();
        }

        @Test
        void validatePassword_lowercaseAndUppercaseLetters_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid("easyPassword", null)).isFalse();
        }

        @Test
        void validatePassword_lowercaseAndUppercaseLettersAndNumbers_isFailed() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid("easyPassword123", null)).isFalse();
        }

        @Test
        void validatePassword_lowercaseAndUppercaseLettersAndNumbersAndSpecialCharacter_isSuccessful() {
            PasswordConstraintValidator validator = new PasswordConstraintValidator();

            Assertions.assertThat(validator.isValid("easyPassword#123", null)).isTrue();
        }
    }

    @Nested
    @DisplayName("Valid Email constraint tests")
    class ValidEmailConstraintTests {
        @Test
        void validateEmail_nullEmailConsideredValid_isFailed() {
            EmailConstraintValidator validator = new EmailConstraintValidator();

            Assertions.assertThat(validator.isValid(null, null)).isFalse();
        }

        @Test
        void validateEmail_invalidShortEmail_isFailed() {
            EmailConstraintValidator validator = new EmailConstraintValidator();

            Assertions.assertThat(validator.isValid("cb", null)).isFalse();
        }

        @Test
        void validateEmail_invalidLongEmail_isFailed() {
            EmailConstraintValidator validator = new EmailConstraintValidator();

            int maxEmailLength = 254;

            Assertions.assertThat(validator.isValid(RandomStringUtils.randomAlphabetic(maxEmailLength), null)).isFalse();
        }

        @Test
        void validateEmail_validEmail_isSuccessful() {
            EmailConstraintValidator validator = new EmailConstraintValidator();

            Assertions.assertThat(validator.isValid("support@czarbank.org", null)).isTrue();
        }

        @Test
        void validateEmail_validLongEmail_isSuccessful() {
            EmailConstraintValidator validator = new EmailConstraintValidator();

            String email = RandomStringUtils.randomAlphabetic(64) + "@" +
                    RandomStringUtils.randomAlphabetic(185) + ".org";

            int maxEmailLength = 254;

            Assertions.assertThat(email).hasSizeLessThanOrEqualTo(maxEmailLength);
            Assertions.assertThat(validator.isValid(email, null)).isTrue();
        }
    }
}
