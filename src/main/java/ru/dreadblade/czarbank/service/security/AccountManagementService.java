package ru.dreadblade.czarbank.service.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.email.MailService;
import ru.dreadblade.czarbank.service.freemarker.FreemarkerTemplateService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountManagementService {
    private static final String VERIFICATION_EMAIL_SUBJECT = "czar-bank account verification";
    private static final String VERIFICATION_EMAIL_SUPPORT_EMAIL_ADDRESS = "support@czarbank.org";
    private static final String VERIFICATION_EMAIL_TEMPLATE_FILENAME = "verification-email-message.ftlh";

    private final UserRepository userRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final MailService mailService;
    private final FreemarkerTemplateService templateService;

    @Value("${czar-bank.security.email-verification-token.expiration-seconds:86400}")
    private Long emailVerificationTokenExpirationSeconds;

    @SneakyThrows
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

            Map<String, Object> templateModel = new HashMap<>();
            templateModel.put("username", userToVerify.getUsername());
            templateModel.put("emailVerificationUrl", emailVerificationUrl);
            templateModel.put("supportEmailAddress", VERIFICATION_EMAIL_SUPPORT_EMAIL_ADDRESS);

            String emailMessageContent = templateService.getProcessedFreemarkerTemplate(VERIFICATION_EMAIL_TEMPLATE_FILENAME, templateModel);

            mailService.sendHtmlMail(userToVerify.getEmail(), VERIFICATION_EMAIL_SUBJECT, emailMessageContent);

            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_VERIFICATION_TOKEN_EXPIRED);
        }


        userToVerify.setEmailVerified(true);

        userRepository.save(userToVerify);
    }
}
