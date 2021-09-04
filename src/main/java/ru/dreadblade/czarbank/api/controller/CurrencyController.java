package ru.dreadblade.czarbank.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreadblade.czarbank.api.mapper.CurrencyMapper;
import ru.dreadblade.czarbank.api.model.response.CurrencyResponseDTO;
import ru.dreadblade.czarbank.service.CurrencyService;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/currencies")
@RestController
public class CurrencyController {
    private final CurrencyService currencyService;
    private final CurrencyMapper currencyMapper;

    @Autowired
    public CurrencyController(CurrencyService currencyService, CurrencyMapper currencyMapper) {
        this.currencyService = currencyService;
        this.currencyMapper = currencyMapper;
    }

    @GetMapping
    public ResponseEntity<List<CurrencyResponseDTO>> findAll() {
        return ResponseEntity.ok(currencyService.findAll().stream()
                .map(currencyMapper::entityToResponseDTO)
                .collect(Collectors.toList()));
    }
}
