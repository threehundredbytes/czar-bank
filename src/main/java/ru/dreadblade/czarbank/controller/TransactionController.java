package ru.dreadblade.czarbank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.service.TransactionService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RequestMapping("/api/transactions")
@RestController
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> findAllTransactions() {
        return ResponseEntity.ok(transactionService.findAll());
    }

    @GetMapping("/{bankAccountId}")
    public ResponseEntity<List<Transaction>> findAllByBankAccountId(@PathVariable Long bankAccountId) {
        return ResponseEntity.ok(transactionService.findAllByBankAccountId(bankAccountId));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(Transaction transaction, HttpServletRequest request) {
        Transaction createdTransaction = transactionService.createTransaction(transaction);
        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdTransaction.getId()))
                .body(createdTransaction);
    }
}
