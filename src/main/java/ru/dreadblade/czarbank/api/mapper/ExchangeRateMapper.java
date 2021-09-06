package ru.dreadblade.czarbank.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.dreadblade.czarbank.api.model.response.ExchangeRateResponseDTO;
import ru.dreadblade.czarbank.domain.ExchangeRate;

@Mapper
public interface ExchangeRateMapper {
    @Mapping(target = "currencyId", source = "currency.id")
    ExchangeRateResponseDTO entityToResponseDTO(ExchangeRate exchangeRate);
}
