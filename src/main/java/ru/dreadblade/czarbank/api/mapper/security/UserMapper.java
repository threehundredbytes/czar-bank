package ru.dreadblade.czarbank.api.mapper.security;

import org.mapstruct.Mapper;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.UserResponseDTO;
import ru.dreadblade.czarbank.domain.security.User;

@Mapper
public interface UserMapper {
    User userRequestToUser(UserRequestDTO userRequestDTO);
    UserResponseDTO userToUserResponseDTO(User user);
}
