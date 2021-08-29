package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokensRequestDTO {
    private String refreshToken;
}
