package ru.dreadblade.czarbank.service.task.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.dreadblade.czarbank.domain.security.BlacklistedAccessToken;
import ru.dreadblade.czarbank.repository.security.BlacklistedAccessTokenRepository;
import ru.dreadblade.czarbank.service.task.Task;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReleaseBlacklistedAccessTokensTask implements Task {

    private final BlacklistedAccessTokenRepository blacklistedAccessTokenRepository;

    @Value("${czar-bank.security.access-token.expiration-seconds}")
    private int accessTokenExpirationSeconds;

    @Scheduled(fixedRateString = "#{${czar-bank.security.access-token.expiration-seconds:900}}", timeUnit = TimeUnit.SECONDS)
    @Override
    public void run() {
        List<BlacklistedAccessToken> blacklistedAccessTokens = blacklistedAccessTokenRepository
                .findAllByCreatedAtIsBefore(Instant.now().minusSeconds(accessTokenExpirationSeconds));

        if (!blacklistedAccessTokens.isEmpty()) {
            blacklistedAccessTokens.forEach(blacklistedAccessToken ->
                    blacklistedAccessTokenRepository.deleteById(blacklistedAccessToken.getId()));
        }

        log.info("Released blacklisted access tokens");
    }
}
