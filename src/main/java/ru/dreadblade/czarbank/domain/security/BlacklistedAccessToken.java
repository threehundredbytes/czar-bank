package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BlacklistedAccessToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blacklisted_access_token_id_sequence")
    private Long id;

    @Column(updatable = false, length = 1024)
    private String accessToken;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}