package ru.dreadblade.czarbank.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.EmailVerificationTokenRepository;

import java.util.UUID;

@Service
public class EmailVerificationTokenService {
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    public EmailVerificationTokenService(EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    public EmailVerificationToken generateVerificationToken(User user) {
        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .emailVerificationToken(generateEmailVerificationToken())
                .user(user)
                .build();

        return emailVerificationTokenRepository.save(emailVerificationToken);
    }

    private String generateEmailVerificationToken() {
        String emailVerificationToken = UUID.randomUUID().toString();

        while (emailVerificationTokenRepository.existsByEmailVerificationToken(emailVerificationToken)) {
            emailVerificationToken = UUID.randomUUID().toString();
        }

        return emailVerificationToken;
    }

    public EmailVerificationToken findByEmailVerificationToken(String emailVerificationToken) {
        return emailVerificationTokenRepository.findByEmailVerificationToken(emailVerificationToken)
                .orElseThrow(() -> new CzarBankSecurityException(ExceptionMessage.INVALID_EMAIL_VERIFICATION_TOKEN));
    }
}
