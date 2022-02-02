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
public class RefreshTokenSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "refresh_token_session_sequence")
    private Long id;

    @Column(updatable = false, unique = true)
    private String refreshToken;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @Builder.Default
    private Boolean isRevoked = false;
}
