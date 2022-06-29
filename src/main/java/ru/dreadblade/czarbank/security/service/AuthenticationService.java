package ru.dreadblade.czarbank.security.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.domain.security.BlacklistedAccessToken;
import ru.dreadblade.czarbank.domain.security.RecoveryCode;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;
import ru.dreadblade.czarbank.repository.security.RecoveryCodeRepository;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;

import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final static int TOTP_CODE_LENGTH = 6;
    private final static int RECOVERY_CODE_LENGTH = 19;
    private final static String TOTP_CODE_REGEXP = "\\d{6}";
    private final static String RECOVERY_CODE_REGEXP = "[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}-[\\d\\w]{4}";

    private final AuthenticationManager authenticationManager;
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
    private final RecoveryCodeRepository recoveryCodeRepository;
    private final TotpService totpService;

    @Value("${czar-bank.security.access-token.header.prefix}")
    private String authorizationHeaderPrefix;

    public User authenticateUser(AuthenticationRequestDTO authenticationRequestDTO) {
        String username = authenticationRequestDTO.getUsername();
        String password = authenticationRequestDTO.getPassword();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(token);
        User user = (User) authentication.getPrincipal();

        if (!user.isEmailVerified()) {
            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_VERIFICATION_REQUIRED);
        }

        if (user.isTwoFactorAuthenticationEnabled()) {
            String code = authenticationRequestDTO.getCode();

            performTwoFactorAuthentication(user, code);
        }

        return user;
    }

    public void logout(String accessToken, String refreshToken) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (accessToken.startsWith(authorizationHeaderPrefix)) {
            accessToken = accessToken.substring(authorizationHeaderPrefix.length());
        }

        BlacklistedAccessToken blacklistedAccessToken = BlacklistedAccessToken.builder()
                .accessToken(accessToken)
                .build();

        RefreshTokenSession refreshTokenSession = refreshTokenSessionRepository.findByRefreshToken(refreshToken)
                .filter(Predicate.not(RefreshTokenSession::getIsRevoked))
                .filter(session -> session.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new CzarBankSecurityException(ExceptionMessage.INVALID_REFRESH_TOKEN));

        blacklistedAccessTokenRepository.save(blacklistedAccessToken);

        refreshTokenSession.setIsRevoked(true);
        refreshTokenSessionRepository.save(refreshTokenSession);
    }

    private void performTwoFactorAuthentication(User user, String code) {
        if (StringUtils.isBlank(code)) {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }

        if (isTotpCode(code)) {
            performTotpAuthentication(user, code);
        } else if (isRecoveryCode(code)) {
            performRecoveryCodeAuthentication(user, code);
        } else {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }
    }

    private void performTotpAuthentication(User user, String totpCode) {
        String secretKey = user.getTwoFactorAuthenticationSecretKey();

        if (!totpService.isValidCode(totpCode, secretKey)) {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }
    }

    private void performRecoveryCodeAuthentication(User user, String code) {
        Optional<RecoveryCode> optionalRecoveryCode = recoveryCodeRepository.findByCodeAndUser(code, user);

        if (optionalRecoveryCode.isPresent()) {
            RecoveryCode recoveryCode = optionalRecoveryCode.get();
            recoveryCode.setIsUsed(true);

            recoveryCodeRepository.save(recoveryCode);
        } else {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
        }
    }

    private boolean isTotpCode(String code) {
        return code.length() == TOTP_CODE_LENGTH && code.matches(TOTP_CODE_REGEXP);
    }

    private boolean isRecoveryCode(String code) {
        return code.length() == RECOVERY_CODE_LENGTH && code.matches(RECOVERY_CODE_REGEXP);
    }
}
