package ru.dreadblade.czarbank.service.security;

import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.TotpService;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {
    private final TotpService totpService;
    private final SecretGenerator secretGenerator;
    private final UserRepository userRepository;

    public byte[] generateQrCodeImageForUser(User user) {
        if (user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
        }

        String secretKey = secretGenerator.generate();
        user.setTwoFactorAuthenticationSecretKey(secretKey);

        userRepository.save(user);

        return totpService.generateQrCodeImage(secretKey, user.getEmail(), "CzarBank");
    }

    public MediaType getQrCodeImageMediaType() {
        return totpService.getQrCodeImageMediaType();
    }

    public void verifyTwoFactorAuthentication(String code, User user) {
        if (user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
        }

        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (StringUtils.isBlank(secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        if (!totpService.isValidCode(code, secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_VERIFICATION_FAILED);
        }

        user.setTwoFactorAuthenticationEnabled(true);
        userRepository.save(user);
    }
}
