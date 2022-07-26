package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.UserResponseDTO;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(uses = { RoleMapper.class })
public interface UserMapper {
    User requestDtoToEntity(UserRequestDTO userRequestDTO);
    UserResponseDTO entityToResponseDto(User user);

    default Set<Role> map(List<Long> rolesIdsList) {
        return rolesIdsList.stream()
                .map(id -> Role.builder()
                        .id(id)
                        .build())
                .collect(Collectors.toSet());
    }
}
