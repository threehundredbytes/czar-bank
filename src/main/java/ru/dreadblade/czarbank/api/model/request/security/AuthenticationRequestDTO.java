package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestDTO {
    @NotBlank(message = "Username must be not empty")
    private String username;

    @NotBlank(message = "Password must be not empty")
    private String password;
}
