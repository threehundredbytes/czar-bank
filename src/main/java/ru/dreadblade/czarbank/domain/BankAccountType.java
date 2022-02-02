package ru.dreadblade.czarbank.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Digits;
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

    private String name;

    @Digits(integer = 0, fraction = 10)
    private BigDecimal transactionCommission;

    @Digits(integer = 0, fraction = 10)
    private BigDecimal currencyExchangeCommission;
}
