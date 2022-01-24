package ru.dreadblade.czarbank.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccountType extends BaseEntity {
    private String name;

    private BigDecimal transactionCommission;

    private BigDecimal currencyExchangeCommission;
}
