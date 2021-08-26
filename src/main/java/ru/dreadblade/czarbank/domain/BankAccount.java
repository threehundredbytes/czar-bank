package ru.dreadblade.czarbank.domain;

import lombok.*;
import ru.dreadblade.czarbank.domain.security.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String number;

    @ManyToOne(fetch = FetchType.EAGER)
    private User owner;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private Boolean isClosed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    private BankAccountType bankAccountType;

    @OneToMany(mappedBy = "sourceBankAccount")
    private Set<Transaction> transactions;
}
