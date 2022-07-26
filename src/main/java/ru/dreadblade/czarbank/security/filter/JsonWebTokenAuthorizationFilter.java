package ru.dreadblade.czarbank.security.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankSecurityException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;
import ru.dreadblade.czarbank.security.service.AccessTokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JsonWebTokenAuthorizationFilter extends OncePerRequestFilter {
    @Value("${czar-bank.security.access-token.header.prefix}")
    private String authorizationHeaderPrefix;

    private final AccessTokenService accessTokenService;
    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    @Autowired
    public JsonWebTokenAuthorizationFilter(AccessTokenService accessTokenService, BlacklistedAccessTokenRepository blacklistedAccessTokenRepository) {
        this.accessTokenService = accessTokenService;
        this.blacklistedAccessTokenRepository = blacklistedAccessTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, AccountStatusException {
        if (request.getServletPath().equals("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isBlank(accessToken) || !accessToken.startsWith(authorizationHeaderPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = accessToken.substring(authorizationHeaderPrefix.length());

        if (blacklistedAccessTokenRepository.existsByAccessToken(accessToken)) {
            throw new CzarBankSecurityException(ExceptionMessage.INVALID_ACCESS_TOKEN);
        }

        User user = accessTokenService.getUserFromToken(accessToken);

        if (!user.isEmailVerified()) {
            throw new CzarBankSecurityException(ExceptionMessage.EMAIL_VERIFICATION_REQUIRED);
        }

        if (user.isAccountLocked()) {
            throw new LockedException("User's account is locked");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("User's account is disabled");
        }

        if (user.isAccountExpired()) {
            throw new AccountExpiredException("User's account is expired");
        }

        if (user.isCredentialsExpired()) {
            throw new CredentialsExpiredException("User's credentials are expired");
        }

        var token = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        token.setDetails(new WebAuthenticationDetails(request));

        SecurityContextHolder.getContext().setAuthentication(token);

        filterChain.doFilter(request, response);
    }
}
