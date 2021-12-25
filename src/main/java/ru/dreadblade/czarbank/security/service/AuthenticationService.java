package ru.dreadblade.czarbank.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.domain.security.RefreshTokenSession;
import ru.dreadblade.czarbank.domain.security.BlacklistedAccessToken;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RefreshTokenSessionRepository;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;

import java.util.function.Predicate;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;
    private final RefreshTokenSessionRepository refreshTokenSessionRepository;

    @Value("${czar-bank.security.json-web-token.access-token.header.prefix}")
    private String authenticationHeaderPrefix;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 BlacklistedAccessTokenRepository blacklistedAccessTokenRepository,
                                 RefreshTokenSessionRepository refreshTokenSessionRepository) {
        this.authenticationManager = authenticationManager;
        this.blacklistedAccessTokenRepository = blacklistedAccessTokenRepository;
        this.refreshTokenSessionRepository = refreshTokenSessionRepository;
    }

    public User authenticateUser(AuthenticationRequestDTO authenticationRequestDTO) {
        String username = authenticationRequestDTO.getUsername();
        String password = authenticationRequestDTO.getPassword();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(token);

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new CzarBankException(ExceptionMessage.USER_NOT_FOUND);
    }

    public void logout(String accessToken, String refreshToken) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (accessToken.startsWith(authenticationHeaderPrefix)) {
            accessToken = accessToken.substring(authenticationHeaderPrefix.length());
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
