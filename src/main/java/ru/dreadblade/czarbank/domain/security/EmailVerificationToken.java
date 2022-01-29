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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EmailVerificationToken extends BaseEntity {
    @Column(updatable = false)
    private String emailVerificationToken;

    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
