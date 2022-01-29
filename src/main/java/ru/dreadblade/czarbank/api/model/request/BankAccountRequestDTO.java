package ru.dreadblade.czarbank.api.model.request;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequestDTO {
    @NotNull(message = "Owner id must be not null", groups = CreateRequest.class)
    private Long ownerId;

    @NotNull(message = "Bank account type id must be not null", groups = CreateRequest.class)
    private Long bankAccountTypeId;

    @NotNull(message = "Used currency id must be not null", groups = CreateRequest.class)
    private Long usedCurrencyId;
}
