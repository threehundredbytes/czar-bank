package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreadblade.czarbank.api.mapper.security.PermissionMapper;
import ru.dreadblade.czarbank.api.model.response.security.PermissionResponseDTO;
import ru.dreadblade.czarbank.service.security.PermissionService;

import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/permissions")
@RestController
public class PermissionController {
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @Autowired
    public PermissionController(PermissionService permissionService, PermissionMapper permissionMapper) {
        this.permissionService = permissionService;
        this.permissionMapper = permissionMapper;
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponseDTO>> findAll() {
        return ResponseEntity.ok(permissionService.findAll().stream()
                .map(permissionMapper::permissionToPermissionResponse)
                .collect(Collectors.toList()));
    }
}
