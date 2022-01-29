package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.RoleResponseDTO;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.domain.security.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = { PermissionMapper.class })
public interface RoleMapper {
    Role requestDtoToEntity(RoleRequestDTO roleRequestDTO);
    RoleResponseDTO entityToResponseDto(Role role);

    default Set<Permission> map(List<Long> permissionsIdList) {
        return permissionsIdList.stream()
                .map(id -> Permission.builder()
                        .id(id)
                        .build())
                .collect(Collectors.toSet());
    }
}
