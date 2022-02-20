package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentRole;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    @NotBlank(message = "Role name must be not empty", groups = CreateRequest.class)
    @NonExistentRole(groups = CreateRequest.class)
    @Size(message = "The role name must be between 3 and 100 characters long (inclusive)",
            min = 3, max = 100, groups = { CreateRequest.class, UpdateRequest.class })
    private String name;

    @Singular(value = "addPermission")
    @NotEmpty(message = "Role must have at least one permission", groups = CreateRequest.class)
    @NotNull(message = "Permissions cannot be null", groups = UpdateRequest.class)
    private List<Long> permissions = Collections.emptyList();
}