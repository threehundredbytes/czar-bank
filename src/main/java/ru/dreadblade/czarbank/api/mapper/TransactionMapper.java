package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.TransactionResponseDTO;
import ru.dreadblade.czarbank.domain.Transaction;

@Mapper
public interface TransactionMapper {
    Transaction requestDtoToEntity(TransactionRequestDTO transactionRequestDTO);
    TransactionResponseDTO entityToResponseDto(Transaction transaction);
}
