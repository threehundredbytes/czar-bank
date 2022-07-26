package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountTypeRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountTypeResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccountType;

@Mapper
public interface BankAccountTypeMapper {
    BankAccountType requestDtoToEntity(BankAccountTypeRequestDTO bankAccountTypeRequestDTO);
    BankAccountTypeResponseDTO entityToResponseDto(BankAccountType bankAccountType);
}
