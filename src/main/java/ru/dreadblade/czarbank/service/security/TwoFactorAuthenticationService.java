package ru.dreadblade.czarbank.service.security;

import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RecoveryCodeRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.security.service.TotpService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static ru.dreadblade.czarbank.exception.ExceptionMessage.*;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationService {
    private final static int TOTP_CODE_LENGTH = 6;
    private final static int RECOVERY_CODE_LENGTH = 19;
    private final static String TOTP_CODE_REGEXP = "\\d{6}";
    private final static String RECOVERY_CODE_REGEXP = "[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}";

    private final TotpService totpService;
    private final SecretGenerator secretGenerator;
    private final UserRepository userRepository;
    private final RecoveryCodeGenerator recoveryCodeGenerator;
    private final RecoveryCodeRepository recoveryCodeRepository;

    @Value("${czar-bank.security.two-factor-authentication.recovery-codes.amount}")
    private int recoveryCodesAmount;

    public byte[] generateQrCodeImageForUser(User user) {
        if (user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
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
            throw new CzarBankSecurityException(TWO_FACTOR_AUTHENTICATION_ALREADY_SETUP);
        }

        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (StringUtils.isBlank(secretKey)) {
            throw new CzarBankSecurityException(SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        if (!totpService.isValidCode(code, secretKey)) {
            throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
        }

        user.setTwoFactorAuthenticationEnabled(true);
        userRepository.save(user);

        return generateRecoveryCodesForUser(user);
    }

    public void performTwoFactorAuthentication(User user, String code) {
        if (StringUtils.isBlank(code)) {
            throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }

        if (isTotpCode(code)) {
            performTotpAuthentication(user, code);
        } else if (isRecoveryCode(code)) {
            performRecoveryCodeAuthentication(user, code);
        } else {
            throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }
    }

    @Transactional
    public void disableTwoFactorAuthentication(String code, User user) {
        if (!user.isTwoFactorAuthenticationEnabled()) {
            throw new CzarBankSecurityException(SETUP_TWO_FACTOR_AUTHENTICATION);
        }

        if (isTotpCode(code)) {
            if (isValidTotpCode(user, code)) {
                disableTwoFactorAuthenticationForUser(user);
            } else {
                throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
            }
        } else if (isRecoveryCode(code)) {
            useRecoveryCode(user, code);

            disableTwoFactorAuthenticationForUser(user);
        } else {
            throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE);
        }
    }

    private void disableTwoFactorAuthenticationForUser(User user) {
        user.setTwoFactorAuthenticationEnabled(false);
        user.setTwoFactorAuthenticationSecretKey(null);
        recoveryCodeRepository.deleteAllByUser(user);

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

    private void performTotpAuthentication(User user, String totpCode) {
        if (!isValidTotpCode(user, totpCode)) {
            throw new CzarBankSecurityException(INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }
    }

    private void performRecoveryCodeAuthentication(User user, String code) {
        validateAndUseRecoveryCode(user, code, INVALID_RECOVERY_CODE_AUTH_FAILED, RECOVERY_CODE_ALREADY_USED_AUTH_FAILED);
    }

    private void useRecoveryCode(User user, String code) {
        validateAndUseRecoveryCode(user, code, INVALID_RECOVERY_CODE, RECOVERY_CODE_ALREADY_USED);
    }

    private void validateAndUseRecoveryCode(
            User user,
            String code,
            ExceptionMessage invalidRecoveryCodeMessage,
            ExceptionMessage recoveryCodeIsUsedMessage
    ) {
        Optional<RecoveryCode> optionalRecoveryCode = recoveryCodeRepository.findByCodeAndUser(code, user);

        if (optionalRecoveryCode.isPresent()) {
            RecoveryCode recoveryCode = optionalRecoveryCode.get();

            if (recoveryCode.getIsUsed()) {
                throw new CzarBankSecurityException(recoveryCodeIsUsedMessage);
            }

            recoveryCode.setIsUsed(true);

            recoveryCodeRepository.save(recoveryCode);
        } else {
            throw new CzarBankSecurityException(invalidRecoveryCodeMessage);
        }
    }

    private boolean isValidTotpCode(User user, String totpCode) {
        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        return totpService.isValidCode(totpCode, secretKey);
    }

    private boolean isTotpCode(String code) {
        return code.length() == TOTP_CODE_LENGTH && code.matches(TOTP_CODE_REGEXP);
    }

    private boolean isRecoveryCode(String code) {
        return code.length() == RECOVERY_CODE_LENGTH && code.matches(RECOVERY_CODE_REGEXP);
    }
}
