package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.security.PermissionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.PermissionResponseDTO;
import ru.dreadblade.czarbank.domain.security.Permission;

@Mapper
public interface PermissionMapper {
    Permission permissionRequestToPermission(PermissionRequestDTO permissionRequestDTO);
    PermissionResponseDTO permissionToPermissionResponse(Permission permission);
}
