package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "Username must be not empty", groups = CreateRequest.class)
    private String username;

    @NotBlank(message = "Email must be not empty", groups = CreateRequest.class)
    private String email;

    @NotBlank(message = "Password must be not empty", groups = CreateRequest.class)
    private String password;

    @NotNull(message = "User roles cannot be null", groups = UpdateRequest.class)
    @Singular(value = "addRole")
    private List<Long> roles = Collections.emptyList();
}
