package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.dreadblade.czarbank.domain.BaseEntity;

import javax.persistence.*;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmailVerificationToken extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_verification_token_id_sequence")
    @SequenceGenerator(name = "email_verification_token_id_sequence", allocationSize = 1)
    private Long id;

    @Column(length = 36, nullable = false, unique = true, updatable = false)
    private String emailVerificationToken;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
