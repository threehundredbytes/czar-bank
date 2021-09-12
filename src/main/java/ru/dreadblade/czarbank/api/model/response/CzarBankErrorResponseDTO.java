package ru.dreadblade.czarbank.api.model.response;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CzarBankErrorResponseDTO {
    @Builder.Default
    private Date timestamp = new Date();
    private int status;
    private String error;
    private String message;
    private String path;
}
