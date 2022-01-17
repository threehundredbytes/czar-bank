package ru.dreadblade.czarbank.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.CurrencyMapper;
import ru.dreadblade.czarbank.api.model.request.CurrencyRequestDTO;
import ru.dreadblade.czarbank.api.model.response.CurrencyResponseDTO;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.service.CurrencyService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
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
                .map(currencyMapper::entityToResponseDto)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('CURRENCY_CREATE')")
    @PostMapping
    public ResponseEntity<CurrencyResponseDTO> createCurrency(@RequestBody CurrencyRequestDTO requestDTO, HttpServletRequest request) {
        String currencyCode = requestDTO.getCode();
        String currencySymbol = requestDTO.getSymbol();

        Currency createdCurrency = currencyService.createCurrency(currencyCode, currencySymbol);

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdCurrency.getId()))
                .body(currencyMapper.entityToResponseDto(createdCurrency));
    }
}
