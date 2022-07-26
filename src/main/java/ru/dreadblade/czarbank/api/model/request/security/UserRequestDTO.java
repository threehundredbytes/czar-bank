package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;
import ru.dreadblade.czarbank.api.validation.constraint.ComplexPassword;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentEmail;
import ru.dreadblade.czarbank.api.validation.constraint.NonExistentUser;
import ru.dreadblade.czarbank.api.validation.constraint.ValidEmail;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    @NotBlank(message = "Username must be not empty", groups = CreateRequest.class)
    @NonExistentUser(groups = CreateRequest.class)
    @Size(message = "The username must be between 3 and 32 characters long (inclusive)",
            min = 3, max = 32, groups = { CreateRequest.class, UpdateRequest.class })
    private String username;

    @NotBlank(message = "Email must be not empty", groups = CreateRequest.class)
    @ValidEmail(groups = { CreateRequest.class, UpdateRequest.class })
    @NonExistentEmail(groups = CreateRequest.class)
    @Size(message = "The email must be between 3 and 254 characters long (inclusive)",
            min = 3, max = 254, groups = { CreateRequest.class, UpdateRequest.class })
    private String email;

    @NotBlank(message = "Password must be not empty", groups = CreateRequest.class)
    @ComplexPassword(groups = { CreateRequest.class, UpdateRequest.class })
    private String password;

    @Singular(value = "addRole")
    @NotNull(message = "User roles cannot be null", groups = UpdateRequest.class)
    private List<Long> roles = Collections.emptyList();
}
