package ru.dreadblade.czarbank.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccountType extends BaseEntity {
    private String name;

    @Digits(integer = 0, fraction = 10)
    private BigDecimal transactionCommission;

    @Digits(integer = 0, fraction = 10)
    private BigDecimal currencyExchangeCommission;
}
