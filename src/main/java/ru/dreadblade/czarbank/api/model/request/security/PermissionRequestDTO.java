package ru.dreadblade.czarbank.api.model.request.security;

import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDTO {
    @NotNull(message = "Permission id must be not empty")
    private Long id;
}