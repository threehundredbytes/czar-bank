package ru.dreadblade.czarbank.security.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.security.service.AccessTokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JsonWebTokenAuthorizationFilter extends OncePerRequestFilter {
    @Value("${czar-bank.security.json-web-token.access-token.header.prefix}")
    private String headerPrefix;

    private final AccessTokenService accessTokenService;

    @Autowired
    public JsonWebTokenAuthorizationFilter(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getServletPath().equals("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isBlank(accessToken) || !accessToken.startsWith(headerPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        accessToken = accessToken.substring(headerPrefix.length());

        User user = accessTokenService.getUserFromToken(accessToken);

        var token = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        token.setDetails(new WebAuthenticationDetails(request));

        SecurityContextHolder.getContext().setAuthentication(token);

        filterChain.doFilter(request, response);
    }
}
