package ru.dreadblade.czarbank.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
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
