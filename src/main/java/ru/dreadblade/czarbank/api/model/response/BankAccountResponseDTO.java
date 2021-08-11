package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponseDTO {
    private Long id;
    private String number;
    private String owner;
    private BigDecimal balance;
}
