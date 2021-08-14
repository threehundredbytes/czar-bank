package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountTypeResponseDTO {
    private Long id;
    private String name;
    private BigDecimal transactionCommission;
}
