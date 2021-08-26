package ru.dreadblade.czarbank.api.model.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequestDTO {
    private Long bankAccountTypeId;
}
