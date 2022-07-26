package ru.dreadblade.czarbank.api.controller.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.model.request.security.TwoFactorAuthenticationCodeRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.RecoveryCodesResponseDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.service.security.AccountManagementService;
import ru.dreadblade.czarbank.service.security.TwoFactorAuthenticationService;

import javax.validation.Valid;

@RequestMapping("/api/account-management")
@RestController
@RequiredArgsConstructor
public class AccountManagementController {
    private final AccountManagementService accountManagementService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/verify-email/{token}")
    public void verifyEmail(@PathVariable String token) {
        accountManagementService.verifyEmail(token);
    }

    @GetMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> setupTwoFactorAuthentication(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok()
                .contentType(twoFactorAuthenticationService.getQrCodeImageMediaType())
                .body(twoFactorAuthenticationService.generateQrCodeImageForUser(user));
    }

    @PostMapping("/2fa/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RecoveryCodesResponseDTO> verifyTwoFactorAuthentication(
            @Valid @RequestBody TwoFactorAuthenticationCodeRequestDTO requestDTO,
            @AuthenticationPrincipal User user
    ) {
        var recoveryCodes = twoFactorAuthenticationService.verifyTwoFactorAuthentication(requestDTO.getCode(), user);

        return ResponseEntity.ok(new RecoveryCodesResponseDTO(recoveryCodes));
    }

    @PostMapping("/2fa/disable")
    @PreAuthorize("isAuthenticated()")
    public void disableTwoFactorAuthentication(@Valid @RequestBody TwoFactorAuthenticationCodeRequestDTO requestDTO,
                                               @AuthenticationPrincipal User user) {
        twoFactorAuthenticationService.disableTwoFactorAuthentication(requestDTO.getCode(), user);
    }
}
