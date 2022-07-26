package ru.dreadblade.czarbank.api.model.request;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountTypeRequestDTO {
    @NotBlank(message = "Bank account type name must be not empty", groups = CreateRequest.class)
    @Size(message = "The maximum length of the bank account type name is 100 characters",
            groups = { CreateRequest.class, UpdateRequest.class }, max = 100)
    private String name;

    @NotNull(message = "Transaction commission must be not null", groups = CreateRequest.class)
    @Digits(message = "Transaction commission must contain 0 integers and up to 6 fractional numbers (inclusive)",
            groups = { CreateRequest.class, UpdateRequest.class }, integer = 0, fraction = 6)
    private BigDecimal transactionCommission;

    @NotNull(message = "Currency exchange commission must be not null", groups = CreateRequest.class)
    @Digits(message = "Currency exchange commission must contain 0 integers and up to 6 fractional numbers (inclusive)",
            groups = { CreateRequest.class, UpdateRequest.class }, integer = 0, fraction = 6)
    private BigDecimal currencyExchangeCommission;
}
