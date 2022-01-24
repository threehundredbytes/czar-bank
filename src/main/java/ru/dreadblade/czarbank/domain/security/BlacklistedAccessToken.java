package ru.dreadblade.czarbank.domain.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BlacklistedAccessToken extends BaseEntity {
    @Column(updatable = false, length = 1024)
    private String accessToken;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}