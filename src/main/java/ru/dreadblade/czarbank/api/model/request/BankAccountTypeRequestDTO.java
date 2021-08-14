package ru.dreadblade.czarbank.api.model.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountTypeRequestDTO {
    private String name;
    private BigDecimal transactionCommission;
}
