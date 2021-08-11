package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.TransactionResponseDTO;
import ru.dreadblade.czarbank.domain.Transaction;

@Mapper
public interface TransactionMapper {
    Transaction transactionRequestToTransaction(TransactionRequestDTO transactionRequestDTO);
    TransactionResponseDTO transactionToTransactionResponse(Transaction transaction);
}
