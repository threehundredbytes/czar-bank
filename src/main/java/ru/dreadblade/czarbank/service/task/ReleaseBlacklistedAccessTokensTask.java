package ru.dreadblade.czarbank.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.dreadblade.czarbank.domain.security.BlacklistedAccessToken;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReleaseBlacklistedAccessTokensTask implements Task {

    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    @Value("${czar-bank.security.json-web-token.access-token.expiration-seconds}")
    private int accessTokenExpirationSeconds;

    @Override
    public boolean execute() {
        List<BlacklistedAccessToken> blacklistedAccessTokens = blacklistedAccessTokenRepository
                .findAllByCreatedAtIsBefore(Instant.now().minusSeconds(accessTokenExpirationSeconds));

        if (!blacklistedAccessTokens.isEmpty()) {
            blacklistedAccessTokens.forEach(blacklistedAccessToken ->
                    blacklistedAccessTokenRepository.deleteById(blacklistedAccessToken.getId()));
        }

        return true;
    }
}
