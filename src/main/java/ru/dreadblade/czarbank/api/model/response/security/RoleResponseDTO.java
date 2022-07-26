package ru.dreadblade.czarbank.api.model.response.security;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDTO {
    private Long id;
    private String name;
    private Set<PermissionResponseDTO> permissions;
}
