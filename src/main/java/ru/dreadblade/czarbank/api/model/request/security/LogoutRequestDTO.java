package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequestDTO {
    private String refreshToken;
}
