package ru.dreadblade.czarbank.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduledTasks {
    private final FetchExchangeRatesFromCbrTask fetchExchangeRatesFromCbrTask;
    private final ReleaseBlacklistedAccessTokensTask releaseBlacklistedAccessTokensTask;

    @Scheduled(fixedRateString = "#{${czar-bank.currency.exchange-rate.update-rate-seconds:86400}}", timeUnit = TimeUnit.SECONDS)
    public void fetchExchangeRatesFromCbr() {
        if (fetchExchangeRatesFromCbrTask.execute()) {
            log.info("Fetching currency exchange rates from the API of the Central Bank of Russia completed successfully");
        } else {
            log.error("Error when fetching currency exchange rates from the API of the Central Bank of Russia");
        }
    }

    @Scheduled(fixedRateString = "#{${czar-bank.security.access-token.expiration-seconds:900}}", timeUnit = TimeUnit.SECONDS)
    public void releaseBlacklistedAccessTokens() {
        if (releaseBlacklistedAccessTokensTask.execute()) {
            log.info("Released blacklisted access tokens");
        } else {
            log.info("Error when releasing blacklisted access tokens");
        }
    }
}
