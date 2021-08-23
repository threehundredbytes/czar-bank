package ru.dreadblade.czarbank.api.controller;

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
import org.springframework.test.context.jdbc.Sql;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.AuthenticationResponseDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.AccessTokenService;

import java.util.concurrent.TimeUnit;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "czar-bank.security.json-web-token.access-token.expiration-seconds=5"})
@DisplayName("Authentication Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    private static final String LOGIN_API_URL = "/api/auth/login";
    private static final String USERS_API_URL = "/api/users";
    private static final String ACCESS_TOKEN_IS_INVALID_MESSAGE = "Access token is invalid";
    private static final String ACCESS_TOKEN_EXPIRED_MESSAGE = "Access token expired";

    @Value("${czar-bank.security.json-web-token.access-token.header.prefix}")
    private String headerPrefix;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccessTokenService accessTokenService;

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
        void accessToken_isValid(String username) throws Exception {
            User user = userRepository.findByUsername(username).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(authenticated());
        }

        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void accessToken_withInvalidSignature_isFailed(String username) throws Exception {
            User user = userRepository.findByUsername(username).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user) + "someCorruption...";

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_IS_INVALID_MESSAGE));
        }

        @ParameterizedTest(name = "#{index} with [{arguments}]")
        @MethodSource("ru.dreadblade.czarbank.api.controller.AuthenticationIntegrationTest#getStreamAllUsers")
        void accessToken_modified_isFailed(String username) throws Exception {
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
        void accessToken_isExpired_isFailed() throws Exception {
            User user = userRepository.findById(BASE_USER_ID + 1L).orElseThrow();

            String accessToken = headerPrefix + accessTokenService.generateAccessToken(user);

            TimeUnit.SECONDS.sleep(6);

            mockMvc.perform(get(USERS_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, accessToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ACCESS_TOKEN_EXPIRED_MESSAGE));
        }
    }
}
