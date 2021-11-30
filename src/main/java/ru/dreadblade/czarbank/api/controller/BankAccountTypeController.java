package ru.dreadblade.czarbank.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.BankAccountTypeMapper;
import ru.dreadblade.czarbank.api.model.request.BankAccountTypeRequestDTO;
import ru.dreadblade.czarbank.api.model.response.BankAccountTypeResponseDTO;
import ru.dreadblade.czarbank.domain.BankAccountType;
import ru.dreadblade.czarbank.service.BankAccountTypeService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/bank-account-types")
@RestController
public class BankAccountTypeController {
    private final BankAccountTypeService bankAccountTypeService;
    private final BankAccountTypeMapper bankAccountTypeMapper;

    @Autowired
    public BankAccountTypeController(BankAccountTypeService bankAccountTypeService, BankAccountTypeMapper bankAccountTypeMapper) {
        this.bankAccountTypeService = bankAccountTypeService;
        this.bankAccountTypeMapper = bankAccountTypeMapper;
    }

    @GetMapping
    public ResponseEntity<List<BankAccountTypeResponseDTO>> findAll() {
        return ResponseEntity.ok(bankAccountTypeService.findAll().stream()
                .map(bankAccountTypeMapper::bankAccountTypeToBankAccountTypeResponse)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('BANK_ACCOUNT_TYPE_CREATE')")
    @PostMapping
    public ResponseEntity<BankAccountTypeResponseDTO> createBankAccountType(
            @RequestBody BankAccountTypeRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        BankAccountType createdType = bankAccountTypeService.create(requestDTO);
        BankAccountTypeResponseDTO responseDTO = bankAccountTypeMapper.bankAccountTypeToBankAccountTypeResponse(createdType);

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdType.getId()))
                .body(responseDTO);
    }

    @PreAuthorize("hasAuthority('BANK_ACCOUNT_TYPE_UPDATE')")
    @PutMapping("/{bankAccountTypeId}")
    public ResponseEntity<BankAccountTypeResponseDTO> updateBankAccountTypeById(
            @PathVariable long bankAccountTypeId,
            @RequestBody BankAccountTypeRequestDTO requestDTO
    ) {
        BankAccountType updatedType = bankAccountTypeService.updateById(bankAccountTypeId, requestDTO);

        return ResponseEntity.ok(bankAccountTypeMapper
                .bankAccountTypeToBankAccountTypeResponse(updatedType));
    }

    @PreAuthorize("hasAuthority('BANK_ACCOUNT_TYPE_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{bankAccountTypeId}")
    public void deleteBankAccountById(@PathVariable long bankAccountTypeId) {
        bankAccountTypeService.deleteById(bankAccountTypeId);
    }
}
