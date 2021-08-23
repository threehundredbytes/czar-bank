package ru.dreadblade.czarbank.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.BankAccountMapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccount;
import ru.dreadblade.czarbank.service.BankAccountService;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/bank-accounts")
@RestController
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final BankAccountMapper bankAccountMapper;

    @Autowired
    public BankAccountController(BankAccountService bankAccountService, BankAccountMapper bankAccountMapper) {
        this.bankAccountService = bankAccountService;
        this.bankAccountMapper = bankAccountMapper;
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponseDTO>> findAll() {
        return ResponseEntity.ok(bankAccountService.findAll().stream()
                .map(bankAccountMapper::bankAccountToBankAccountResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountResponseDTO> findById(@PathVariable Long accountId) {
        BankAccount bankAccount = bankAccountService.findById(accountId);
        BankAccountResponseDTO responseDTO = bankAccountMapper.bankAccountToBankAccountResponse(bankAccount);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public ResponseEntity<BankAccountResponseDTO> createAccount(@RequestBody BankAccountRequestDTO bankAccount, HttpServletRequest request) {
        BankAccount createdAccount = bankAccountService.create(bankAccount.getOwner());
        BankAccountResponseDTO responseDTO = bankAccountMapper.bankAccountToBankAccountResponse(createdAccount);

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdAccount.getId()))
                .body(responseDTO);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{accountId}")
    public void deleteAccountById(@PathVariable Long accountId) {
        bankAccountService.deleteById(accountId);
    }
}