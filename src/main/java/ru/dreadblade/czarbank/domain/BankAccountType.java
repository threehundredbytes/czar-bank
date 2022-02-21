package ru.dreadblade.czarbank.domain;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccountType extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bank_account_type_id_sequence")
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 6, scale = 6)
    private BigDecimal transactionCommission;

    @Column(nullable = false, precision = 6, scale = 6)
    private BigDecimal currencyExchangeCommission;
}
