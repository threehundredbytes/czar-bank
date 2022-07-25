package ru.dreadblade.czarbank.api.controller;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.spring.autoconfigure.TotpProperties;
import dev.samstevens.totp.time.TimeProvider;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.dreadblade.czarbank.util.RecoveryCodeTestUtils;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.LogoutRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.RefreshTokensRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.AuthenticationResponseDTO;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;
import ru.dreadblade.czarbank.repository.security.RecoveryCodeRepository;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.AccessTokenService;
import ru.dreadblade.czarbank.security.service.RefreshTokenService;
import ru.dreadblade.czarbank.service.task.scheduled.ReleaseBlacklistedAccessTokensScheduledTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "czar-bank.security.access-token.expiration-seconds=1",
        "czar-bank.security.refresh-token.expiration-seconds=1",
        "czar-bank.security.refresh-token.limit-per-user=5",
        "czar-bank.security.two-factor-authentication.recovery-codes.amount=16"
})
@DisplayName("Authentication Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationIntegrationTest extends BaseIntegrationTest {
    private static final String LOGIN_API_URL = "/api/auth/login";
    private static final String REFRESH_TOKENS_API_URL = "/api/auth/refresh-tokens";
    private static final String LOGOUT_API_URL = "/api/auth/logout";
    private static final String USERS_API_URL = "/api/users";
    private static final String ACCESS_TOKEN_IS_INVALID_MESSAGE = "Invalid access token";
    private static final String ACCESS_TOKEN_EXPIRED_MESSAGE = "Expired access token";
    private static final int ACCESS_TOKEN_EXPIRATION_SECONDS = 1;
    private static final int REFRESH_TOKEN_EXPIRATION_SECONDS = 1;

    @Value("${czar-bank.security.access-token.header.prefix}")
    private String headerPrefix;

    @Value("${czar-bank.security.refresh-token.limit-per-user}")
    private int refreshTokensPerUser;

    @Value("${czar-bank.security.two-factor-authentication.recovery-codes.amount}")
    private int recoveryCodesAmount;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessTokenService accessTokenService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Autowired
    BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    @Autowired
    RecoveryCodeRepository recoveryCodeRepository;

    @Autowired
    RecoveryCodeGenerator recoveryCodeGenerator;

    @Autowired
    ReleaseBlacklistedAccessTokensScheduledTask releaseBlacklistedAccessTokensScheduledTask;

    @Autowired
    SecretGenerator secretGenerator;

    @Autowired
    CodeGenerator codeGenerator;

    @Autowired
    TimeProvider timeProvider;

    @Autowired
    TotpProperties totpProperties;

    @Nested
    @DisplayName("login() Tests")
    class LoginTests {
        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void login_isSuccessful(String username, String password) throws Exception {
            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            String responseContent = mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String accessToken = objectMapper.readValue(responseContent, AuthenticationResponseDTO.class)
                    .getAccessToken();

            User userFromToken = accessTokenService.getUserFromToken(accessToken);

            assertThat(userFromToken.getUsername()).isEqualTo(username);
        }

        @Test
        @Rollback
        void login_withTwoFactorAuthenticationRequired_usingTotpCode_isSuccessful() throws Exception {
            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            String secretKey = secretGenerator.generate();
            long counter = Math.floorDiv(timeProvider.getTime(), totpProperties.getTime().getPeriod());
            String generatedTotpCode = codeGenerator.generate(secretKey, counter);

            currentUser.setTwoFactorAuthenticationSecretKey(secretKey);
            currentUser.setTwoFactorAuthenticationEnabled(true);

            userRepository.save(currentUser);

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("admin")
                    .password("password")
                    .code(generatedTotpCode)
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            String responseContent = mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String accessToken = objectMapper.readValue(responseContent, AuthenticationResponseDTO.class)
                    .getAccessToken();

            User userFromToken = accessTokenService.getUserFromToken(accessToken);

            assertThat(userFromToken.getId()).isEqualTo(currentUser.getId());
        }

        @Test
        @Transactional
        void login_withTwoFactorAuthenticationRequired_usingRecoveryCode_isSuccessful() throws Exception {
            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            currentUser.setTwoFactorAuthenticationEnabled(true);

            List<RecoveryCode> generatedRecoveryCodes = Arrays.stream(recoveryCodeGenerator.generateCodes(recoveryCodesAmount))
                    .map(recoveryCode -> RecoveryCode.builder()
                            .code(recoveryCode)
                            .user(currentUser)
                            .build()).collect(Collectors.toList());

            assertThat(generatedRecoveryCodes).hasSize(recoveryCodesAmount);
            recoveryCodeRepository.saveAll(generatedRecoveryCodes);
            userRepository.save(currentUser);

            RecoveryCode recoveryCode = generatedRecoveryCodes.get(0);

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("admin")
                    .password("password")
                    .code(recoveryCode.getCode())
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            String responseContent = mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String accessToken = objectMapper.readValue(responseContent, AuthenticationResponseDTO.class)
                    .getAccessToken();

            User userFromToken = accessTokenService.getUserFromToken(accessToken);

            assertThat(userFromToken.getId()).isEqualTo(currentUser.getId());
            assertThat(recoveryCode.getIsUsed()).isTrue();
        }

        @Test
        @Rollback
        void login_withTwoFactorAuthenticationRequired_usingWrongTotpCode_isFailed() throws Exception {
            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            String secretKey = secretGenerator.generate();

            long counter = Math.floorDiv(timeProvider.getTime(), totpProperties.getTime().getPeriod());
            String generatedTotpCode = codeGenerator.generate(secretKey, counter);
            String wrongTotpCode = String.valueOf(Integer.parseInt(generatedTotpCode) + 1); // always wrong

            currentUser.setTwoFactorAuthenticationSecretKey(secretKey);
            currentUser.setTwoFactorAuthenticationEnabled(true);

            userRepository.save(currentUser);

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("admin")
                    .password("password")
                    .code(wrongTotpCode)
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED.getMessage()));
        }

        @Test
        @Rollback
        void login_withTwoFactorAuthenticationRequired_usingWrongRecoveryCode_isFailed() throws Exception {
            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            String secretKey = secretGenerator.generate();
            currentUser.setTwoFactorAuthenticationSecretKey(secretKey);
            currentUser.setTwoFactorAuthenticationEnabled(true);

            userRepository.save(currentUser);

            List<RecoveryCode> generatedRecoveryCodes = Arrays.stream(recoveryCodeGenerator.generateCodes(recoveryCodesAmount))
                    .map(recoveryCode -> RecoveryCode.builder()
                            .code(recoveryCode)
                            .user(currentUser)
                            .build()).collect(Collectors.toList());

            recoveryCodeRepository.saveAll(generatedRecoveryCodes);

            String randomRecoveryCode = RecoveryCodeTestUtils.generateRandomRecoveryCode();

            while (recoveryCodeRepository.findByCodeAndUser(randomRecoveryCode, currentUser).isPresent()) {
                randomRecoveryCode = RecoveryCodeTestUtils.generateRandomRecoveryCode();
            }

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("admin")
                    .password("password")
                    .code(randomRecoveryCode)
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED.getMessage()));
        }

        @Test
        @Rollback
        void login_withTwoFactorAuthenticationRequired_emptyTwoFactorAuthenticationTotpCode_isFailed() throws Exception {
            User currentUser = userRepository.findByUsername("admin").orElseThrow();

            String secretKey = secretGenerator.generate();
            currentUser.setTwoFactorAuthenticationSecretKey(secretKey);
            currentUser.setTwoFactorAuthenticationEnabled(true);

            userRepository.save(currentUser);

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("admin")
                    .password("password")
                    .code("")
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED.getMessage()));
        }

        @Test
        void login_isBadCredentials() throws Exception {
            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("someUser")
                    .password("somePass")
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isUnauthorized());
        }

        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        @Transactional
        void login_emailVerificationRequired_isFailed(String username, String password) throws Exception {
            User currentUser = userRepository.findByUsername(username).orElseThrow();

            currentUser.setEmailVerified(false);

            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username(username)
                    .password(password)
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_VERIFICATION_REQUIRED.getMessage()));
        }

        @DisplayName("Validation Tests")
        @Nested
        class ValidationTests {
            @Test
            void login_withEmptyRequestFields_validationIsFailed_responseIsCorrect() throws Exception {
                AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                        .username("")
                        .password("")
                        .build();

                String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

                mockMvc.perform(post(LOGIN_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("username", "password")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Username must be not empty", "Password must be not empty")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(LOGIN_API_URL));
            }
        }
    }

    @Nested
    @DisplayName("Access Token Tests")
    class AccessTokenTests {

        /**
         * @see <a href="https://github.com/spring-projects/spring-security/issues/4516">GitHub Issue</a>
         */
        @Disabled
        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void accessToken_isValid_authIsSuccessful(String username) throws Exception {
            User user = userRepository.findByUsername(username).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(authenticated());
        }

        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void accessToken_withInvalidSignature_authIsFailed(String username) throws Exception {
            User user = userRepository.findByUsername(username).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user) + "someCorruption...";

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_IS_INVALID_MESSAGE));
        }

        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void accessToken_modified_authIsFailed(String username) throws Exception {
            User user = userRepository.findByUsername(username).orElseThrow();

            String accessTokenBeforeCorruption = accessTokenService.generateAccessToken(user);

            int cut = 20;

            String accessToken = headerPrefix + accessTokenBeforeCorruption.substring(0, cut) + "some data..." +
                    accessTokenBeforeCorruption.substring(cut);

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_IS_INVALID_MESSAGE));
        }

        @Test
        void accessToken_isExpired_authIsFailed() throws Exception {
            User user = userRepository.findById(1L).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            TimeUnit.SECONDS.sleep(ACCESS_TOKEN_EXPIRATION_SECONDS + 1);

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED_MESSAGE));
        }

        @Test
        @Transactional
        void accessToken_thenLockingTheUser_authIsFailed() throws Exception {
            long testUserId = 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            assertThat(user.isAccountLocked()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setAccountLocked(true);

            assertThat(user.isAccountLocked()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is locked"));
        }

        @Test
        @Transactional
        void accessToken_thenDisablingTheUser_authIsFailed() throws Exception {
            long testUserId = 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            assertThat(user.isEnabled()).isTrue();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setEnabled(false);

            assertThat(user.isEnabled()).isFalse();

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is disabled"));
        }

        @Test
        @Transactional
        void accessToken_thenExpiringTheUsersAccount_authIsFailed() throws Exception {
            long testUserId = 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            assertThat(user.isAccountExpired()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setAccountExpired(true);

            assertThat(user.isAccountExpired()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is expired"));
        }

        @Test
        @Transactional
        void accessToken_thenExpiringTheUsersCredentials_authIsFailed() throws Exception {
            long testUserId = 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            assertThat(user.isCredentialsExpired()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setCredentialsExpired(true);

            assertThat(user.isCredentialsExpired()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's credentials are expired"));
        }

        @Test
        @Transactional
        void accessToken_thenUndoEmailVerification_authIsFailed() throws Exception {
            long testUserId = 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            assertThat(user.isEmailVerified()).isTrue();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setEmailVerified(false);

            assertThat(user.isEmailVerified()).isFalse();

            mockMvc.perform(get(USERS_API_URL)
                            .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_VERIFICATION_REQUIRED.getMessage()));
        }
    }

    @Nested
    @DisplayName("refreshTokens() Tests")
    class RefreshTokenTests {
        @Test
        @Transactional
        void refreshTokens_refreshTokenIsValid_isSuccessful() throws Exception {
            long testUserId = 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            String responseContent = mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isString())
                    .andExpect(jsonPath("$.refreshToken").isString())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            RefreshTokenSession revokedSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow();

            assertThat(revokedSession.getRefreshToken()).isEqualTo(refreshToken);
            assertThat(revokedSession.getUser()).isEqualTo(user);
            assertThat(revokedSession.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(revokedSession.getIsRevoked()).isTrue();

            AuthenticationResponseDTO responseDTO = objectMapper.readValue(responseContent, AuthenticationResponseDTO.class);

            assertThat(user).isEqualTo(accessTokenService.getUserFromToken(responseDTO.getAccessToken()));

            RefreshTokenSession createdSession = refreshTokenSessionRepository
                    .findByRefreshToken(responseDTO.getRefreshToken()).orElseThrow();

            assertThat(createdSession.getUser()).isEqualTo(user);
            assertThat(createdSession.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            assertThat(createdSession.getIsRevoked()).isFalse();
        }

        @Test
        @Rollback
        void refreshToken_refreshTokenLimit_isSuccessful() {
            long testUserId = 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            int currentRepetition = 0;

            while (currentRepetition < refreshTokensPerUser + 1) {
                currentRepetition++;

                String refreshToken = refreshTokenService.generateRefreshToken(user);
                assertThat(refreshTokenSessionRepository.existsByRefreshToken(refreshToken)).isTrue();

                int refreshTokenSessionsCount = Math.toIntExact(refreshTokenSessionRepository.countByUser(user));

                if (currentRepetition <= refreshTokensPerUser) {
                    assertThat(refreshTokenSessionsCount).isEqualTo(currentRepetition);
                } else {
                    assertThat(refreshTokenSessionsCount).isOne();
                }
            }
        }

        @Test
        @Transactional
        void refreshToken_refreshTokenIsRevoked_isFailed() throws Exception {
            long testUserId = 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            RefreshTokenSession revokedSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow();

            assertThat(revokedSession.getUser()).isEqualTo(user);

            revokedSession.setIsRevoked(true);

            mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Test
        @Transactional
        void refreshToken_refreshTokenIsModified_isFailed() throws Exception {
            long testUserId = 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            String modifiedRefreshToken = refreshToken + RandomStringUtils.randomAlphanumeric(1, 10);

            assertThat(modifiedRefreshToken).isNotEqualTo(refreshToken);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(modifiedRefreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Test
        void refreshToken_refreshTokenIsExpired_isFailed() throws Exception {
            long testUserId = 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            TimeUnit.SECONDS.sleep(REFRESH_TOKEN_EXPIRATION_SECONDS);

            RefreshTokenSession refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow();

            assertThat(refreshTokenSession.getIsRevoked()).isFalse();

            mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.REFRESH_TOKEN_EXPIRED.getMessage()));
        }

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {
            @Test
            void refreshTokens_withEmptyRequestFields_validationIsFailed_responseIsValid() throws Exception {
                RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO("");

                String requestContent = objectMapper.writeValueAsString(requestDTO);

                mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("refreshToken")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Refresh token must be not empty")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(REFRESH_TOKENS_API_URL));
            }
        }
    }


    @Nested
    @DisplayName("logout() Tests")
    class LogoutTests {
        @Test
        @Rollback
        void logout_isSuccessful() throws Exception {
            Long userId = 1L;

            User user = userRepository.findById(userId).orElseThrow();

            String accessToken = accessTokenService.generateAccessToken(user);
            String refreshToken = refreshTokenService.generateRefreshToken(user);

            assertThat(accessTokenService.getUserFromToken(accessToken).getId()).isEqualTo(userId);

            RefreshTokenSession refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken).orElseThrow();
            assertThat(refreshTokenSession.getIsRevoked()).isFalse();

            LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(logoutRequestDTO);

            String accessTokenWithPrefix = headerPrefix + accessToken;

            mockMvc.perform(post(LOGOUT_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, accessTokenWithPrefix)
                            .content(requestContent))
                    .andExpect(status().isOk());

            assertThat(blacklistedAccessTokenRepository.existsByAccessToken(accessToken)).isTrue();

            refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken).orElseThrow();
            assertThat(refreshTokenSession.getIsRevoked()).isTrue();

            mockMvc.perform(post(LOGOUT_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, accessTokenWithPrefix)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.INVALID_ACCESS_TOKEN.getMessage()));

            TimeUnit.SECONDS.sleep(ACCESS_TOKEN_EXPIRATION_SECONDS);

            releaseBlacklistedAccessTokensScheduledTask.run();
            assertThat(blacklistedAccessTokenRepository.existsByAccessToken(accessToken)).isFalse();
        }

        @Test
        @Rollback
        void logout_revokedRefreshToken_isFailed() throws Exception {
            Long userId = 1L;

            User user = userRepository.findById(userId).orElseThrow();

            String accessToken = accessTokenService.generateAccessToken(user);
            String refreshToken = refreshTokenService.generateRefreshToken(user);

            assertThat(accessTokenService.getUserFromToken(accessToken).getId()).isEqualTo(userId);

            RefreshTokenSession refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken).orElseThrow();
            refreshTokenSession.setIsRevoked(true);
            assertThat(refreshTokenSession.getIsRevoked()).isTrue();

            refreshTokenSessionRepository.save(refreshTokenSession);

            LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(logoutRequestDTO);

            String accessTokenWithPrefix = headerPrefix + accessToken;

            mockMvc.perform(post(LOGOUT_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, accessTokenWithPrefix)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Test
        void logout_invalidRefreshToken_isFailed() throws Exception {
            Long userId = 1L;

            User user = userRepository.findById(userId).orElseThrow();

            String accessToken = accessTokenService.generateAccessToken(user);
            String refreshToken = "someRandomRefreshToken";

            assertThat(accessTokenService.getUserFromToken(accessToken).getId()).isEqualTo(userId);

            LogoutRequestDTO logoutRequestDTO = new LogoutRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(logoutRequestDTO);

            String accessTokenWithPrefix = headerPrefix + accessToken;

            mockMvc.perform(post(LOGOUT_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, accessTokenWithPrefix)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.INVALID_REFRESH_TOKEN.getMessage()));
        }

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {
            @Test
            void logout_withEmptyRequestFields_validationIsFailed_responseIsValid() throws Exception {
                LogoutRequestDTO requestDTO = new LogoutRequestDTO("");

                String requestContent = objectMapper.writeValueAsString(requestDTO);

                mockMvc.perform(post(LOGOUT_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestContent))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("refreshToken")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Refresh token must be not empty")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(LOGOUT_API_URL));
            }
        }
    }

    @Nested
    @DisplayName("Request validation tests")
    class RequestValidationTests {
        @Test
        void login_withInvalidJsonInRequestBody_isFailed_responseIsValid() throws Exception {
            String requestContent = "{\"username\":\"someUsername\"\"password\":\"somePassword\"}"; // without comma

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                    .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                    .andExpect(jsonPath("$.message").value("Invalid request body syntax"))
                    .andExpect(jsonPath("$.path").value(LOGIN_API_URL));
        }

        @Test
        void login_withUnsupportedHttpMediaType_responseIsValid() throws Exception {
            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("someUsername")
                    .password("somePassword")
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            String message = "Content type «" + MediaType.TEXT_PLAIN_VALUE + "» not supported!";

            mockMvc.perform(post(LOGIN_API_URL)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content(requestContent))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()))
                    .andExpect(jsonPath("$.error").value(HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase()))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.path").value(LOGIN_API_URL));
        }

        @Test
        void login_withUnsupportedHttpRequestMethod_responseIsValid() throws Exception {
            AuthenticationRequestDTO authenticationRequestDTO = AuthenticationRequestDTO.builder()
                    .username("someUsername")
                    .password("somePassword")
                    .build();

            String requestContent = objectMapper.writeValueAsString(authenticationRequestDTO);

            String message = "Request method «" + RequestMethod.PATCH + "» not supported! Supported methods are: «" +
                    RequestMethod.POST + "»";

            mockMvc.perform(patch(LOGIN_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestContent))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                    .andExpect(jsonPath("$.status").value(HttpStatus.METHOD_NOT_ALLOWED.value()))
                    .andExpect(jsonPath("$.error").value(HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase()))
                    .andExpect(jsonPath("$.message").value(message))
                    .andExpect(jsonPath("$.path").value(LOGIN_API_URL));
        }
    }
}