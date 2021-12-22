package ru.dreadblade.czarbank.repository.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class RevokedAccessTokenRepository {

    @Value("${czar-bank.security.json-web-token.access-token.expiration-seconds}")
    private Long timeToLive;

    private static final Long ADDITIONAL_DURATION = 60L;

    private final ValueOperations<String, Object> valueOperations;

    @Autowired
    public RevokedAccessTokenRepository(RedisTemplate<String, Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    public boolean existsByAccessToken(String accessToken) {
        return valueOperations.get(accessToken) != null;
    }

    public void save(String accessToken) {
        long timeout = timeToLive + ADDITIONAL_DURATION;

        valueOperations.set(accessToken, true, timeout, TimeUnit.SECONDS);
    }
}
