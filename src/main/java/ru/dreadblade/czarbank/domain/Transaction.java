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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant datetime;

    @Column(updatable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount destinationBankAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount sourceBankAccount;
}
