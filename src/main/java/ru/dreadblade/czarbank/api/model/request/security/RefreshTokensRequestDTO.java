package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokensRequestDTO {
    @NotBlank(message = "Refresh token must be not empty")
    private String refreshToken;
}
