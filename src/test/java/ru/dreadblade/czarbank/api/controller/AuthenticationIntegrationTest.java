package ru.dreadblade.czarbank.api.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
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
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.RefreshTokensRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.AuthenticationResponseDTO;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.AccessTokenService;
import ru.dreadblade.czarbank.security.service.RefreshTokenService;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "czar-bank.security.json-web-token.access-token.expiration-seconds=5",
        "czar-bank.security.json-web-token.refresh-token.expiration-seconds=5",
})
@DisplayName("Authentication Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    private static final String LOGIN_API_URL = "/api/auth/login";
    private static final String REFRESH_TOKENS_API_URL = "/api/auth/refresh-tokens";
    private static final String USERS_API_URL = "/api/users";
    private static final String ACCESS_TOKEN_IS_INVALID_MESSAGE = "Access token is invalid";
    private static final String ACCESS_TOKEN_EXPIRED_MESSAGE = "Access token expired";

    @Value("${czar-bank.security.json-web-token.access-token.header.prefix}")
    private String headerPrefix;

    @Value("${czar-bank.security.json-web-token.refresh-token.expiration-seconds}")
    private int refreshTokensPerUser;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessTokenService accessTokenService;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Nested
    @DisplayName("login() Tests")
    class loginTests {
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

            Assertions.assertThat(userFromToken.getUsername()).isEqualTo(username);
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
    }

    @Nested
    @DisplayName("Access Token Tests")
    class accessTokenTests {

        /**
         * Disabled due to https://github.com/spring-projects/spring-security/issues/4516
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
            User user = userRepository.findById(BASE_USER_ID + 1L).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            TimeUnit.SECONDS.sleep(6);

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED_MESSAGE));
        }

        @Test
        @Transactional
        void accessToken_thenLockingTheUser_authIsFailed() throws Exception {
            long testUserId = BASE_USER_ID + 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            Assertions.assertThat(user.isAccountLocked()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setAccountLocked(true);

            Assertions.assertThat(user.isAccountLocked()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is locked"));
        }

        @Test
        @Transactional
        void accessToken_thenDisablingTheUser_authIsFailed() throws Exception {
            long testUserId = BASE_USER_ID + 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            Assertions.assertThat(user.isEnabled()).isTrue();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setEnabled(false);

            Assertions.assertThat(user.isEnabled()).isFalse();

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is disabled"));
        }

        @Test
        @Transactional
        void accessToken_thenExpiringTheUsersAccount_authIsFailed() throws Exception {
            long testUserId = BASE_USER_ID + 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            Assertions.assertThat(user.isAccountExpired()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setAccountExpired(true);

            Assertions.assertThat(user.isAccountExpired()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's account is expired"));
        }

        @Test
        @Transactional
        void accessToken_thenExpiringTheUsersCredentials_authIsFailed() throws Exception {
            long testUserId = BASE_USER_ID + 1L;

            User user = userRepository.findById(testUserId).orElseThrow();

            Assertions.assertThat(user.isCredentialsExpired()).isFalse();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            user.setCredentialsExpired(true);

            Assertions.assertThat(user.isCredentialsExpired()).isTrue();

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("User's credentials are expired"));
        }
    }

    @Nested
    @DisplayName("refreshTokens() Tests")
    class refreshTokenTests {
        @Test
        @Transactional
        void refreshTokens_refreshTokenIsValid_isSuccessful() throws Exception {
            long testUserId = BASE_USER_ID + 1L;
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

            Assertions.assertThat(revokedSession.getRefreshToken()).isEqualTo(refreshToken);
            Assertions.assertThat(revokedSession.getUser()).isEqualTo(user);
            Assertions.assertThat(revokedSession.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            Assertions.assertThat(revokedSession.getIsRevoked()).isTrue();

            AuthenticationResponseDTO responseDTO = objectMapper.readValue(responseContent, AuthenticationResponseDTO.class);

            Assertions.assertThat(user).isEqualTo(accessTokenService.getUserFromToken(responseDTO.getAccessToken()));

            RefreshTokenSession createdSession = refreshTokenSessionRepository
                    .findByRefreshToken(responseDTO.getRefreshToken()).orElseThrow();

            Assertions.assertThat(createdSession.getUser()).isEqualTo(user);
            Assertions.assertThat(createdSession.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
            Assertions.assertThat(createdSession.getIsRevoked()).isFalse();
        }

        @Test
        @Rollback
        void refreshToken_refreshTokenLimit_isSuccessful() throws Exception {
            long testUserId = BASE_USER_ID + 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            int currentRepetition = 0;

            while (currentRepetition < refreshTokensPerUser + 1) {
                currentRepetition++;

                String refreshToken = refreshTokenService.generateRefreshToken(user);

                Assertions.assertThat(refreshTokenSessionRepository.existsByRefreshToken(refreshToken)).isTrue();

                int refreshTokenSessionsCount = Math.toIntExact(refreshTokenSessionRepository.countByUser(user));

                if (currentRepetition <= refreshTokensPerUser) {
                    Assertions.assertThat(refreshTokenSessionsCount).isEqualTo(currentRepetition);
                } else {
                    Assertions.assertThat(refreshTokenSessionsCount).isOne();
                }
            }
        }

        @Test
        @Transactional
        void refreshToken_refreshTokenIsRevoked_isFailed() throws Exception {
            long testUserId = BASE_USER_ID + 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            RefreshTokenSession revokedSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow();

            Assertions.assertThat(revokedSession.getUser()).isEqualTo(user);

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
            long testUserId = BASE_USER_ID + 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            String modifiedRefreshToken = refreshToken + RandomStringUtils.randomAlphanumeric(1, 10);

            Assertions.assertThat(modifiedRefreshToken).isNotEqualTo(refreshToken);

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
            long testUserId = BASE_USER_ID + 1L;
            User user = userRepository.findById(testUserId).orElseThrow();

            String refreshToken = refreshTokenService.generateRefreshToken(user);

            RefreshTokensRequestDTO requestDTO = new RefreshTokensRequestDTO(refreshToken);

            String requestContent = objectMapper.writeValueAsString(requestDTO);

            TimeUnit.SECONDS.sleep(6);

            RefreshTokenSession refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                    .orElseThrow();

            Assertions.assertThat(refreshTokenSession.getIsRevoked()).isFalse();

            mockMvc.perform(post(REFRESH_TOKENS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestContent))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.REFRESH_TOKEN_EXPIRED.getMessage()));
        }
    }
}