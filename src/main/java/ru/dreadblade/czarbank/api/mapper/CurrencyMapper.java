package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.response.CurrencyResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;

@Mapper
public interface CurrencyMapper {
    CurrencyResponseDTO entityToResponseDTO(Currency currency);
}
