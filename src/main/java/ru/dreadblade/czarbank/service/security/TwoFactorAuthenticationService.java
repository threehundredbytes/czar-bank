package ru.dreadblade.czarbank.service.security;

import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RecoveryCodeRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.TotpService;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {
    @Value("${czar-bank.security.two-factor-authentication.recovery-codes.amount}")
    private int recoveryCodesAmount;

    private final TotpService totpService;
    private final SecretGenerator secretGenerator;
    private final UserRepository userRepository;
    private final RecoveryCodeGenerator recoveryCodeGenerator;
    private final RecoveryCodeRepository recoveryCodeRepository;

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

    public List<String> verifyTwoFactorAuthentication(String code, User user) {
        if (user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(ExceptionMessage.TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
        }

        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (StringUtils.isBlank(secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        if (!totpService.isValidCode(code, secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
        }

        user.setTwoFactorAuthenticationEnabled(true);
        userRepository.save(user);

        return generateRecoveryCodesForUser(user);
    }

    public void disableTwoFactorAuthentication(String code, User user) {
        if (!user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(ExceptionMessage.SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (!totpService.isValidCode(code, secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
        }

        user.setTwoFactorAuthenticationEnabled(false);
        user.setTwoFactorAuthenticationSecretKey(null);
        userRepository.save(user);
    }

    private List<String> generateRecoveryCodesForUser(User user) {
        List<String> generatedRecoveryCodes = Arrays.stream(recoveryCodeGenerator.generateCodes(recoveryCodesAmount)).toList();

        recoveryCodeRepository.saveAll(generatedRecoveryCodes.stream()
                .map(code -> RecoveryCode.builder()
                        .code(code)
                        .user(user)
                        .build())
                .toList());

        return generatedRecoveryCodes;
    }
}
