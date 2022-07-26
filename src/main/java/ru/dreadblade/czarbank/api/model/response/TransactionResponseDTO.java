package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private Instant datetime;
    private BigDecimal amount;
    private BigDecimal receivedAmount;
    private BankAccountResponseDTO sourceBankAccount;
    private BankAccountResponseDTO destinationBankAccount;
}
