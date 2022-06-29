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
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;

import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;
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
            String totpCode = authenticationRequestDTO.getCode();
            String secretKey = user.getTwoFactorAuthenticationSecretKey();

            if (StringUtils.isBlank(totpCode) || !totpService.isValidCode(totpCode, secretKey)) {
                throw new CzarBankSecurityException(ExceptionMessage.INVALID_TWO_FACTOR_AUTHENTICATION_CODE_AUTH_FAILED);
            }
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
}
