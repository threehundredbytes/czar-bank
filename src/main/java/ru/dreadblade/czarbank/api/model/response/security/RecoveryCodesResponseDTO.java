package ru.dreadblade.czarbank.api.model.response.security;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecoveryCodesResponseDTO {
    private List<String> recoveryCodes;
}
