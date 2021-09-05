package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import ru.dreadblade.czarbank.repository.CurrencyRepository;
import ru.dreadblade.czarbank.repository.ExchangeRateRepository;
import ru.dreadblade.czarbank.service.ExchangeRateService;

import java.util.concurrent.TimeUnit;

@SpringBootTest(properties = {
        "czar-bank.currency.exchange-rate.update-rate-in-millis=5000"
})
@DisplayName("ExchangeRate Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ExchangeRateIntegrationTest extends BaseIntegrationTest {

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @SpyBean
    ExchangeRateService exchangeRateService;

    @Test
    void fetchExchangeRatesFromCentralBankOfRussia_runsAndIsSuccessful() {
        Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                Mockito.verify(exchangeRateService, Mockito.atLeastOnce()).fetchExchangeRatesFromCentralBankOfRussia());

        Assertions.assertThat(exchangeRateRepository.count()).isGreaterThanOrEqualTo(currencyRepository.count() - 1L);
    }
}
