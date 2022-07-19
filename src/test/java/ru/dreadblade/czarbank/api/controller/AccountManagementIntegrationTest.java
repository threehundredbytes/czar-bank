package ru.dreadblade.czarbank.api.controller;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.spring.autoconfigure.TotpProperties;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.security.TwoFactorAuthenticationCodeRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.EmailVerificationTokenRepository;
import ru.dreadblade.czarbank.repository.security.RecoveryCodeRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.TotpService;
import ru.dreadblade.czarbank.service.security.EmailVerificationTokenService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "czar-bank.security.email-verification-token.expiration-seconds=1",
        "czar-bank.security.two-factor-authentication.recovery-codes.amount=16"
})
@DisplayName("Account Management Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AccountManagementIntegrationTest extends BaseIntegrationTest {

    private static final String VERIFY_EMAIL_API_URL = "/api/account-management/verify-email";
    private static final String USERS_API_URL = "/api/users";
    private static final String SETUP_2FA_API_URL = "/api/account-management/2fa/setup";
    private static final String VERIFY_2FA_API_URL = "/api/account-management/2fa/verify";
    private static final String DISABLE_2FA_API_URL = "/api/account-management/2fa/disable";
    private static final String RECOVERY_CODE_REGEXP = "[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}";
    private static final int EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS = 1;

    @Value("${czar-bank.security.two-factor-authentication.recovery-codes.amount}")
    private int recoveryCodesAmount;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    RecoveryCodeRepository recoveryCodeRepository;

    @Autowired
    TotpService totpService;

    @Autowired
    CodeGenerator codeGenerator;

    @Autowired
    TimeProvider timeProvider;

    @Autowired
    CodeVerifier codeVerifier;

    @Autowired
    SecretGenerator secretGenerator;

    @Autowired
    TotpProperties totpProperties;

    @Autowired
    RecoveryCodeGenerator recoveryCodeGenerator;

    @Nested
    @DisplayName("verifyEmail() Tests")
    class VerifyEmailTests {
        @Test
        @Transactional
        void verifyEmail_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            assertThat(createdUser.isEmailVerified()).isFalse();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isOk());

            assertThat(createdUser.isEmailVerified()).isTrue();
        }

        @Test
        @Transactional
        void verifyEmail_emailVerificationTokenExpired_resendEmailVerificationToken_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());


            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            assertThat(createdUser.isEmailVerified()).isFalse();

            TimeUnit.SECONDS.sleep(EMAIL_VERIFICATION_TOKEN_EXPIRATION_SECONDS + 1);

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken expiredEmailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + expiredEmailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_VERIFICATION_TOKEN_EXPIRED.getMessage()));

            assertThat(createdUser.isEmailVerified()).isFalse();

            emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            assertThat(emailVerificationTokensForUser).hasSize(2);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(1);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isOk());

            assertThat(createdUser.isEmailVerified()).isTrue();
        }

        @Test
        @Transactional
        void verifyEmail_emailVerificationToken_isInvalid_isFailed() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            assertThat(createdUser.isEmailVerified()).isFalse();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            assertThat(emailVerificationTokensForUser).hasSize(1);

            String emailVerificationToken = "aRandomStaffThatCantBeValid";

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.INVALID_EMAIL_VERIFICATION_TOKEN.getMessage()));

            assertThat(createdUser.isEmailVerified()).isFalse();
        }

        @Test
        @Transactional
        void verifyEmail_userHasAlreadyVerifiedTheirEmail_isFailed() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            assertThat(createdUser.isEmailVerified()).isFalse();

            createdUser.setEmailVerified(true);

            assertThat(createdUser.isEmailVerified()).isTrue();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_ADDRESS_ALREADY_VERIFIED.getMessage()));

        }
    }

    @Nested
    @DisplayName("Two-factor authentication tests")
    class TwoFactorAuthenticationTests {
        @Nested
        @DisplayName("setupTwoFactorAuthentication() and verifyTwoFactorAuthentication() tests")
        class setupAndVerifyTwoFactorAuthenticationTests {
            @Test
            @Rollback
            @WithUserDetails("admin")
            void setupAndVerifyTwoFactorAuthentication_isSuccessful() throws Exception {
                mockMvc.perform(get(SETUP_2FA_API_URL))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(totpService.getQrCodeImageMediaType()));

                User currentUser = userRepository.findByUsername("admin").orElseThrow();

                String secretKey = currentUser.getTwoFactorAuthenticationSecretKey();
                long counter = Math.floorDiv(timeProvider.getTime(), totpProperties.getTime().getPeriod());

                String generatedTotpCode = codeGenerator.generate(secretKey, counter);

                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(generatedTotpCode);

                mockMvc.perform(post(VERIFY_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.recoveryCodes", hasSize(recoveryCodesAmount)))
                        .andExpect(jsonPath("$.recoveryCodes[*]", everyItem(matchesRegex(RECOVERY_CODE_REGEXP))));

                currentUser = userRepository.findByUsername("admin").orElseThrow();
                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isTrue();
            }

            @Test
            @Transactional
            @WithUserDetails("admin")
            void setupTwoFactorAuthentication_alreadySetup_isFailed() throws Exception {
                User currentUser = userRepository.findByUsername("admin").orElseThrow();
                currentUser.setTwoFactorAuthenticationEnabled(true);

                mockMvc.perform(get(SETUP_2FA_API_URL))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP.getMessage()));
            }

            @Test
            @Rollback
            @WithUserDetails("admin")
            void setupAndVerifyTwoFactorAuthentication_wrongCode_isFailed() throws Exception {
                mockMvc.perform(get(SETUP_2FA_API_URL))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(totpService.getQrCodeImageMediaType()));

                String randomWrongTotpCode = RandomStringUtils.randomNumeric(7); // always wrong
                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(randomWrongTotpCode);

                mockMvc.perform(post(VERIFY_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED.getMessage()));

                User currentUser = userRepository.findByUsername("admin").orElseThrow();
                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isFalse();
                assertThat(currentUser.getTwoFactorAuthenticationSecretKey()).isNotBlank();
            }

            @Test
            @Transactional
            @WithUserDetails("admin")
            void setupAndVerifyTwoFactorAuthentication_alreadySetup_isFailed() throws Exception {
                User currentUser = userRepository.findByUsername("admin").orElseThrow();
                currentUser.setTwoFactorAuthenticationEnabled(true);

                String randomTotpCode = RandomStringUtils.randomNumeric(6);
                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(randomTotpCode);

                mockMvc.perform(post(VERIFY_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP.getMessage()));
            }

            @Test
            @WithUserDetails("admin")
            void verifyTwoFactorAuthentication_withoutSetup_isFailed() throws Exception {
                String randomTotpCode = RandomStringUtils.randomNumeric(6);

                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(randomTotpCode);

                mockMvc.perform(post(VERIFY_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION.getMessage()));

                User currentUser = userRepository.findByUsername("admin").orElseThrow();
                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isFalse();
                assertThat(currentUser.getTwoFactorAuthenticationSecretKey()).isNullOrEmpty();
            }

            @DisplayName("Validation Tests")
            @Nested
            class validationTests {
                @Test
                @Transactional
                @WithUserDetails("admin")
                void verifyTwoFactorAuthentication_withNullCode_validationIsFailed_responseIsCorrect() throws Exception {
                    User currentUser = userRepository.findByUsername("admin").orElseThrow();

                    String secretKey = secretGenerator.generate();

                    currentUser.setTwoFactorAuthenticationSecretKey(secretKey);

                    var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(null);

                    mockMvc.perform(post(VERIFY_2FA_API_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDTO)))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                            .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                            .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                            .andExpect(jsonPath("$.errors", hasSize(1)))
                            .andExpect(jsonPath("$.errors[0].field").value("code"))
                            .andExpect(jsonPath("$.errors[0].message").value("Two-factor authentication code must be not empty"))
                            .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                            .andExpect(jsonPath("$.path").value(VERIFY_2FA_API_URL));

                    currentUser = userRepository.findByUsername("admin").orElseThrow();
                    assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isFalse();
                }
            }
        }

        @Nested
        @DisplayName("disableTwoFactorAuthentication() tests")
        class disableTwoFactorAuthenticationTests {
            @Test
            @Transactional
            @WithUserDetails("admin")
            void disableTwoFactorAuthentication_isSuccessful() throws Exception {
                User currentUser = userRepository.findByUsername("admin").orElseThrow();

                String secretKey = secretGenerator.generate();
                currentUser.setTwoFactorAuthenticationEnabled(true);
                currentUser.setTwoFactorAuthenticationSecretKey(secretKey);

                List<RecoveryCode> generatedRecoveryCodes = Arrays.stream(recoveryCodeGenerator.generateCodes(recoveryCodesAmount))
                        .map(recoveryCode -> RecoveryCode.builder()
                                .code(recoveryCode)
                                .user(currentUser)
                                .build()).collect(Collectors.toList());

                assertThat(generatedRecoveryCodes).hasSize(recoveryCodesAmount);
                recoveryCodeRepository.saveAll(generatedRecoveryCodes);

                long expectedRecoveryCodesAmount = recoveryCodesAmount;

                assertThat(recoveryCodeRepository.count()).isEqualTo(expectedRecoveryCodesAmount);

                long counter = Math.floorDiv(timeProvider.getTime(), totpProperties.getTime().getPeriod());
                String generatedTotpCode = codeGenerator.generate(secretKey, counter);

                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(generatedTotpCode);

                mockMvc.perform(post(DISABLE_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isOk());

                expectedRecoveryCodesAmount = 0;

                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isFalse();
                assertThat(recoveryCodeRepository.count()).isEqualTo(expectedRecoveryCodesAmount);
            }

            @Test
            @Rollback
            @WithUserDetails("admin")
            void disableTwoFactorAuthentication_withoutSetup_isFailed() throws Exception {
                String randomTotpCode = RandomStringUtils.randomNumeric(6);

                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(randomTotpCode);

                mockMvc.perform(post(DISABLE_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION.getMessage()));

                User currentUser = userRepository.findByUsername("admin").orElseThrow();
                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isFalse();
            }

            @Test
            @Transactional
            @WithUserDetails("admin")
            void disableTwoFactorAuthentication_wrongCode_isFailed() throws Exception {
                User currentUser = userRepository.findByUsername("admin").orElseThrow();

                String secretKey = secretGenerator.generate();
                currentUser.setTwoFactorAuthenticationEnabled(true);
                currentUser.setTwoFactorAuthenticationSecretKey(secretKey);

                String randomWrongTotpCode = RandomStringUtils.randomNumeric(7); // always wrong

                var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(randomWrongTotpCode);

                mockMvc.perform(post(DISABLE_2FA_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message")
                                .value(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE.getMessage()));

                assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isTrue();
                assertThat(currentUser.getTwoFactorAuthenticationSecretKey()).isNotBlank();
            }

            @DisplayName("Validation Tests")
            @Nested
            class validationTests {
                @Test
                @Transactional
                @WithUserDetails("admin")
                void disableTwoFactorAuthentication_withNullCode_validationIsFailed_responseIsCorrect() throws Exception {
                    User currentUser = userRepository.findByUsername("admin").orElseThrow();

                    String secretKey = secretGenerator.generate();

                    currentUser.setTwoFactorAuthenticationEnabled(true);
                    currentUser.setTwoFactorAuthenticationSecretKey(secretKey);

                    var requestDTO = new TwoFactorAuthenticationCodeRequestDTO(null);

                    mockMvc.perform(post(DISABLE_2FA_API_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(requestDTO)))
                            .andExpect(status().isUnprocessableEntity())
                            .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                            .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                            .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                            .andExpect(jsonPath("$.errors", hasSize(1)))
                            .andExpect(jsonPath("$.errors[0].field").value("code"))
                            .andExpect(jsonPath("$.errors[0].message").value("Two-factor authentication code must be not empty"))
                            .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                            .andExpect(jsonPath("$.path").value(DISABLE_2FA_API_URL));

                    currentUser = userRepository.findByUsername("admin").orElseThrow();
                    assertThat(currentUser.isTwoFactorAuthenticationEnabled()).isTrue();
                }
            }
        }
    }
}
