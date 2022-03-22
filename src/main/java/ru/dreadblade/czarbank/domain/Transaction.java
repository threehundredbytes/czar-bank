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
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_sequence")
    @SequenceGenerator(name = "transaction_id_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, updatable = false, precision = 20, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, precision = 20, scale = 2)
    private BigDecimal receivedAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount sourceBankAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccount destinationBankAccount;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
