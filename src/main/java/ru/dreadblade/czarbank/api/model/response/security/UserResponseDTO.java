package ru.dreadblade.czarbank.api.model.response.security;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String userId;
    private String username;
    private String email;
    private Set<RoleResponseDTO> roles;
}
