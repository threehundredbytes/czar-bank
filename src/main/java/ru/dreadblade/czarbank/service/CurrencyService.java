package ru.dreadblade.czarbank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.Currency;
import ru.dreadblade.czarbank.repository.CurrencyRepository;

import java.util.List;

@Service
public class CurrencyService {
    private final CurrencyRepository currencyRepository;

    @Autowired
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    public List<Currency> findAll() {
        return currencyRepository.findAll();
    }
}
