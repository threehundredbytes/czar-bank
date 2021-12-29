package ru.dreadblade.czarbank.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;

@Service
public class AccountManagementService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;

    @Autowired
    public AccountManagementService(UserRepository userRepository, EmailVerificationTokenService emailVerificationTokenService) {
        this.userRepository = userRepository;
        this.emailVerificationTokenService = emailVerificationTokenService;
    }

    public void verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.findByEmailVerificationToken(token);

        User userToVerify = emailVerificationToken.getUser();

        if (userToVerify.isEmailVerified()) {
            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_ADDRESS_ALREADY_VERIFIED);
        }

        userToVerify.setEmailVerified(true);

        userRepository.save(userToVerify);
    }
}
