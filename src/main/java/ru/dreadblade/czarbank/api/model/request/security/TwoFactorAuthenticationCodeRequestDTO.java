package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthenticationCodeRequestDTO {
    @NotBlank(message = "Two-factor authentication code must be not empty")
    private String code;
}
