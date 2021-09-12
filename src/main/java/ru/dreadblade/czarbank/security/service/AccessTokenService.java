package ru.dreadblade.czarbank.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccessTokenService {
    @Value("${czar-bank.security.json-web-token.access-token.issuer}")
    private String issuer;

    @Value("${czar-bank.security.json-web-token.access-token.audience}")
    private String audience;

    @Value("${czar-bank.security.json-web-token.access-token.expiration-seconds}")
    private int expirationSeconds;

    private String secretKey;

    private final UserRepository userRepository;
    private final JWTVerifier verifier;

    public AccessTokenService(@Value("${czar-bank.security.json-web-token.access-token.secret-key}") String secretKey,
                              UserRepository userRepository) {
        this.userRepository = userRepository;
        this.secretKey = secretKey;
        this.verifier = JWT.require(Algorithm.HMAC512(secretKey)).build();
    }

    public String generateAccessToken(User user) {
        List<String> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date issuedAt = new Date();
        Date expiresAt = new Date(System.currentTimeMillis() + expirationSeconds * 1000L);

        return JWT.create()
                .withIssuer(issuer)
                .withAudience(audience)
                .withSubject(user.getUsername())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("authorities", authorities)
                .sign(Algorithm.HMAC512(secretKey));
    }

    public User getUserFromToken(String accessToken) {
        DecodedJWT decodedJWT = verifier.verify(accessToken);

        String username = decodedJWT.getSubject();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));
    }
}
