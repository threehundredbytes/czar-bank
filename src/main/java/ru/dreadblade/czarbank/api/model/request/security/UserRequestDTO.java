package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String username;
    private String email;
    private String password;
    private Set<RoleRequestDTO> roles;
}
