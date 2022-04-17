package ru.dreadblade.czarbank.api.controller.security;

import dev.samstevens.totp.exceptions.QrGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.security.service.TwoFactorAuthenticationService;
import ru.dreadblade.czarbank.service.security.AccountManagementService;

@RequestMapping("/api/account-management")
@RestController
public class AccountManagementController {
    private final AccountManagementService accountManagementService;
    private final TwoFactorAuthenticationService twoFactorAuthenticationService;

    @Autowired
    public AccountManagementController(AccountManagementService accountManagementService, TwoFactorAuthenticationService twoFactorAuthenticationService) {
        this.accountManagementService = accountManagementService;
        this.twoFactorAuthenticationService = twoFactorAuthenticationService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/verify-email/{token}")
    public void verifyEmail(@PathVariable String token) {
        accountManagementService.verifyEmail(token);
    }

    @GetMapping("/2fa/setup")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> setupTwoFactorAuthentication(@AuthenticationPrincipal User user) throws QrGenerationException {
        return ResponseEntity.ok()
                .contentType(twoFactorAuthenticationService.getQrCodeImageMediaType())
                .body(twoFactorAuthenticationService.generateQrCodeImageForUser(user));
    }

    @PostMapping("/2fa/verify/{code}")
    @PreAuthorize("isAuthenticated()")
    public void verifyTwoFactorAuthentication(@PathVariable String code, @AuthenticationPrincipal User user) {
        twoFactorAuthenticationService.verifyCodeForUser(code, user);
    }
}
