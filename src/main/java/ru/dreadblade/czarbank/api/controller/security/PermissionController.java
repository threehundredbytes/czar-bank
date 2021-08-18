package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.service.security.PermissionService;

import java.util.List;

@RequestMapping("/api/permissions")
@RestController
public class PermissionController {
    private final PermissionService permissionService;

    @Autowired
    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<Permission>> findAll() {
        return ResponseEntity.ok(permissionService.findAll());
    }
}
