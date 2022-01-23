package ru.dreadblade.czarbank.domain;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Transaction extends BaseEntity {
    @CreationTimestamp
    @Column(updatable = false)
    private Instant datetime;

    @Column(updatable = false)
    private BigDecimal amount;

    @Column(updatable = false)
    private BigDecimal receivedAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount sourceBankAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount destinationBankAccount;
}
