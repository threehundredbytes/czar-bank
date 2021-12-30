package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.service.security.AccountManagementService;

@RequestMapping("/api/")
@RestController
public class AccountManagementController {
    private final AccountManagementService accountManagementService;

    @Autowired
    public AccountManagementController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/account-management/verify-email/{token}")
    public void verifyEmail(@PathVariable String token) {
        accountManagementService.verifyEmail(token);
    }
}
