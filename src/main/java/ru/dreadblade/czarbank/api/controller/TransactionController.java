package ru.dreadblade.czarbank.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.TransactionMapper;
import ru.dreadblade.czarbank.api.model.request.TransactionRequestDTO;
import ru.dreadblade.czarbank.api.model.response.TransactionResponseDTO;
import ru.dreadblade.czarbank.domain.Transaction;
import ru.dreadblade.czarbank.service.TransactionService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/transactions")
@RestController
public class TransactionController {
    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Autowired
    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> findAllTransactions() {
        return ResponseEntity.ok(transactionService.findAll().stream()
                .map(transactionMapper::transactionToTransactionResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{bankAccountId}")
    public ResponseEntity<List<TransactionResponseDTO>> findAllByBankAccountId(@PathVariable Long bankAccountId) {
        return ResponseEntity.ok(transactionService.findAllByBankAccountId(bankAccountId).stream()
                .map(transactionMapper::transactionToTransactionResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(TransactionRequestDTO transactionRequest, HttpServletRequest request) {
        Transaction transaction = transactionMapper.transactionRequestToTransaction(transactionRequest);

        Transaction createdTransaction = transactionService.createTransaction(transaction);

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdTransaction.getId()))
                .body(transactionMapper.transactionToTransactionResponse(createdTransaction));
    }
}
