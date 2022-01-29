package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.dreadblade.czarbank.api.model.response.BankAccountResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;

@Mapper
public interface BankAccountMapper {
    @Mappings({
            @Mapping(target = "ownerId", source = "owner.id"),
            @Mapping(target = "usedCurrencyId", source = "usedCurrency.id"),
            @Mapping(target = "bankAccountTypeId", source = "bankAccountType.id")
    })
    BankAccountResponseDTO entityToResponseDto(BankAccount bankAccount);
}
