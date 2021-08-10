package ru.dreadblade.czarbank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.service.BankAccountService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RequestMapping("/api/bank-accounts")
@RestController
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    public ResponseEntity<List<BankAccount>> getAll() {
        return ResponseEntity.ok(bankAccountService.getAll());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccount> findById(@PathVariable Long accountId) {
        return ResponseEntity.ok(bankAccountService.findById(accountId));
    }

    @PostMapping
    public ResponseEntity<BankAccount> createAccount(@RequestBody BankAccount bankAccount, HttpServletRequest request) {
        BankAccount createdAccount = bankAccountService.create(bankAccount.getOwner());
        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdAccount.getId()))
                .body(createdAccount);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{accountId}")
    public void deleteAccountById(@PathVariable Long accountId) {
        bankAccountService.deleteById(accountId);
    }
}