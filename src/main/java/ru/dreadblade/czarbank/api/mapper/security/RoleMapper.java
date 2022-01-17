package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.RoleResponseDTO;
import ru.dreadblade.czarbank.domain.security.Role;

@Mapper(uses = { PermissionMapper.class })
public interface RoleMapper {
    Role requestDtoToEntity(RoleRequestDTO roleRequestDTO);
    RoleResponseDTO entityToResponseDto(Role role);
}
