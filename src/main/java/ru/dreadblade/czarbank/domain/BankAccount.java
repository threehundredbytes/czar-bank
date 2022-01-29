package ru.dreadblade.czarbank.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.dreadblade.czarbank.domain.security.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccount extends BaseEntity {
    private String number;

    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    private Currency usedCurrency;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private Boolean isClosed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccountType bankAccountType;

    @OneToMany(mappedBy = "sourceBankAccount")
    private Set<Transaction> transactions;
}
