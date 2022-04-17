package ru.dreadblade.czarbank.security.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {
    private final UserRepository userRepository;
    private final SecretGenerator secretGenerator;
    private final QrDataFactory qrDataFactory;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;

    public byte[] generateQrCodeImageForUser(User user) throws QrGenerationException {
        if (user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
        }

        String secret = secretGenerator.generate();

        user.setTwoFactorAuthenticationSecretKey(secret);

        QrData data = qrDataFactory.newBuilder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("CzarBank")
                .build();

        userRepository.save(user);

        return qrGenerator.generate(data);
    }

    public MediaType getQrCodeImageMediaType() {
        return MediaType.parseMediaType(qrGenerator.getImageMimeType());
    }

    public void verifyCodeForUser(String code, User user) {
        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (StringUtils.isEmpty(secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        if (codeVerifier.isValidCode(secretKey, code)) {
            user.setTwoFactorAuthenticationEnabled(true);
            userRepository.save(user);
        } else {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
        }
    }
}
