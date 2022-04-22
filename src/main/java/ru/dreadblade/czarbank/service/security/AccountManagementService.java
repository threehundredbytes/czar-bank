package ru.dreadblade.czarbank.service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.MailService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountManagementService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final MailService mailService;

    @Value("${czar-bank.security.email-verification-token.expiration-seconds:86400}")
    private Long emailVerificationTokenExpirationSeconds;

    public void verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByEmailVerificationToken(token);

        User userToVerify = emailVerificationToken.getUser();

        if (userToVerify.isEmailVerified()) {
            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_ADDRESS_ALREADY_VERIFIED);
        }

        if (emailVerificationToken.getCreatedAt().isBefore(Instant.now().minusSeconds(emailVerificationTokenExpirationSeconds))) {
            emailVerificationToken = emailVerificationTokenService.generateVerificationToken(userToVerify);

            String emailVerificationUrl = ServletUriComponentsBuilder
                    .fromCurrentRequestUri()
                    .replacePath("/api/account-management/verify-email/")
                    .toUriString() + emailVerificationToken.getEmailVerificationToken();

            String emailSubject = "Email verification";
            String emailMessageContent = "Hello, " + userToVerify.getUsername() +
                    "!\nTo verify your account, please, follow the link below:\n" + emailVerificationUrl +
                    "\nIf you have any questions, please, let us know\nContact us: support@czarbank.org";

            mailService.sendMail(userToVerify.getEmail(), emailSubject, emailMessageContent);

            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_VERIFICATION_TOKEN_EXPIRED);
        }


        userToVerify.setEmailVerified(true);

        userRepository.save(userToVerify);
    }
}
