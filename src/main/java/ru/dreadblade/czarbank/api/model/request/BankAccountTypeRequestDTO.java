package ru.dreadblade.czarbank.api.model.request;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountTypeRequestDTO {
    @NotBlank(message = "Bank account type name must be not empty", groups = CreateRequest.class)
    @Length(message = "The maximum length of the bank account type name is 32 characters",
            groups = { CreateRequest.class, UpdateRequest.class }, max = 32)
    private String name;

    @NotNull(message = "Transaction commission must be not null", groups = CreateRequest.class)
    @Digits(message = "Transaction commission must contain 0 integers and up to 10 fractional numbers (inclusive)",
            groups = { CreateRequest.class, UpdateRequest.class }, integer = 0, fraction = 10)
    private BigDecimal transactionCommission;

    @NotNull(message = "Currency exchange commission must be not null", groups = CreateRequest.class)
    @Digits(message = "Currency exchange commission must contain 0 integers and up to 10 fractional numbers (inclusive)",
            groups = { CreateRequest.class, UpdateRequest.class }, integer = 0, fraction = 10)
    private BigDecimal currencyExchangeCommission;
}
