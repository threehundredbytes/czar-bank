package ru.dreadblade.czarbank.api.model.request;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    @NotNull(message = "Transaction amount must be not null", groups = CreateRequest.class)
    private BigDecimal amount;

    @NotBlank(message = "Source bank account number must be not empty", groups = CreateRequest.class)
    @Size(message = "The length of the source bank account number must be 20 characters",
            groups = CreateRequest.class, min = 20, max = 20)
    private String sourceBankAccountNumber;

    @NotBlank(message = "Destination bank account number must be not empty", groups = CreateRequest.class)
    @Size(message = "The length of the destination bank account number must be 20 characters",
            groups = CreateRequest.class, min = 20, max = 20)
    private String destinationBankAccountNumber;
}
