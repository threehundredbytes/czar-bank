package ru.dreadblade.czarbank.security.config;

import dev.samstevens.totp.code.HashingAlgorithm;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwoFactorAuthenticationConfiguration {
    @Bean
    public HashingAlgorithm hashingAlgorithm() {
        return HashingAlgorithm.SHA256;
    }
}
