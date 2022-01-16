package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    @NotBlank(message = "Role name must be not empty", groups = CreateRequest.class)
    private String name;

    @NotEmpty(message = "Role must have at least one permission", groups = CreateRequest.class)
    @Valid
    private List<PermissionRequestDTO> permissions;
}