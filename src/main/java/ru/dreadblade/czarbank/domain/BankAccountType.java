package ru.dreadblade.czarbank.domain;

import lombok.*;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class BankAccountType extends BaseEntity {
    private String name;

    private BigDecimal transactionCommission;

    private BigDecimal currencyExchangeCommission;
}
