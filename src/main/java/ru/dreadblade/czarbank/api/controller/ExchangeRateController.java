package ru.dreadblade.czarbank.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.ExchangeRateMapper;
import ru.dreadblade.czarbank.api.model.response.ExchangeRateResponseDTO;
import ru.dreadblade.czarbank.service.ExchangeRateService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/currencies/exchange-rates")
@RestController
@RequiredArgsConstructor
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService;
    private final ExchangeRateMapper exchangeRateMapper;

    @GetMapping("/latest")
    public List<ExchangeRateResponseDTO> findAllLatest() {
        return exchangeRateService.findAllLatest().stream()
                .map(exchangeRateMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/historical/{date}")
    public List<ExchangeRateResponseDTO> findAllByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return exchangeRateService.findAllByDate(date).stream()
                .map(exchangeRateMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/time-series")
    public List<ExchangeRateResponseDTO> findAllInTimeSeries(
            @RequestParam("start-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("end-date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return exchangeRateService.findAllInTimeSeries(startDate, endDate).stream()
                .map(exchangeRateMapper::entityToResponseDto)
                .collect(Collectors.toList());
    }
}
