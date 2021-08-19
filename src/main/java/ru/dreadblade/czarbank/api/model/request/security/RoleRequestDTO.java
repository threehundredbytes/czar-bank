package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    private String name;
    private Set<PermissionRequestDTO> permissions;
}
