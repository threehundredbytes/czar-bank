package ru.dreadblade.czarbank.api.model.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private BigDecimal amount;
    private String sourceBankAccountNumber;
    private String destinationBankAccountNumber;
}
