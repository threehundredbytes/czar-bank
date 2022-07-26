package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.response.security.PermissionResponseDTO;
import ru.dreadblade.czarbank.domain.security.Permission;

@Mapper
public interface PermissionMapper {
    PermissionResponseDTO entityToResponseDto(Permission permission);
}
