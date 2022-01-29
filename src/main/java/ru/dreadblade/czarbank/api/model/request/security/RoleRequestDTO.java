package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collections;
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
    @NotNull(message = "Permissions cannot be null", groups = UpdateRequest.class)
    @Singular(value = "addPermission")
    private List<Long> permissions = Collections.emptyList();
}