package ru.dreadblade.czarbank.api.model.response.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponseDTO {
    private String accessToken;
    private String refreshToken;
}
