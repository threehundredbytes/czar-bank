package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;

@Mapper
public interface BankAccountMapper {
    BankAccount bankAccountRequestToBankAccount(BankAccountRequestDTO bankAccountRequestDTO);
    BankAccountResponseDTO bankAccountToBankAccountResponse(BankAccount bankAccount);
}
