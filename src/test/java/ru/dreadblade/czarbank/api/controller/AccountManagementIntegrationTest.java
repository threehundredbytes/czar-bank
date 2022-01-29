package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.EmailVerificationTokenRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.security.EmailVerificationTokenService;

import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "czar-bank.security.email-verification-token.expiration-seconds=5",
})
@DisplayName("Account Management Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AccountManagementIntegrationTest extends BaseIntegrationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    EmailVerificationTokenRepository emailVerificationTokenRepository;

    @MockBean
    MailSender mailSender;

    private static final String VERIFY_EMAIL_API_URL = "/api/account-management/verify-email";

    private static final String USERS_API_URL = "/api/users";

    @Nested
    @DisplayName("verifyEmail() Tests")
    class VerifyEmailTests {
        @Test
        @Transactional
        void verifyEmail_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            Assertions.assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isOk());

            Assertions.assertThat(createdUser.isEmailVerified()).isTrue();
        }

        @Test
        @Transactional
        void verifyEmail_emailVerificationTokenExpired_resendEmailVerificationToken_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();

            TimeUnit.SECONDS.sleep(6);

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            Assertions.assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken expiredEmailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + expiredEmailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_VERIFICATION_TOKEN_EXPIRED.getMessage()));

            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();

            Mockito.verify(mailSender, Mockito.times(2)).send(Mockito.any(SimpleMailMessage.class));

            emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            Assertions.assertThat(emailVerificationTokensForUser).hasSize(2);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(1);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isOk());

            Assertions.assertThat(createdUser.isEmailVerified()).isTrue();
        }

        @Test
        @Transactional
        void verifyEmail_emailVerificationToken_isInvalid_isFailed() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            Assertions.assertThat(emailVerificationTokensForUser).hasSize(1);

            String emailVerificationToken = "aRandomStaffThatCantBeValid";

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.INVALID_EMAIL_VERIFICATION_TOKEN.getMessage()));

            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();
        }

        @Test
        @Transactional
        void verifyEmail_userHasAlreadyVerifiedTheirEmail_isFailed() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();
            Assertions.assertThat(createdUser.isEmailVerified()).isFalse();

            createdUser.setEmailVerified(true);

            Assertions.assertThat(createdUser.isEmailVerified()).isTrue();

            var emailVerificationTokensForUser = emailVerificationTokenRepository.findAllByUser(createdUser);
            Assertions.assertThat(emailVerificationTokensForUser).hasSize(1);

            EmailVerificationToken emailVerificationToken = emailVerificationTokensForUser.get(0);

            mockMvc.perform(get(VERIFY_EMAIL_API_URL + "/" + emailVerificationToken.getEmailVerificationToken()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.EMAIL_ADDRESS_ALREADY_VERIFIED.getMessage()));

        }
    }
}
