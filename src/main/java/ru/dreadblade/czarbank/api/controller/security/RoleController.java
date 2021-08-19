package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.security.RoleMapper;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.RoleResponseDTO;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.service.security.RoleService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/roles")
@RestController
public class RoleController {
    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @Autowired
    public RoleController(RoleService roleService, RoleMapper roleMapper) {
        this.roleService = roleService;
        this.roleMapper = roleMapper;
    }

    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> findAll() {
        return ResponseEntity.ok(roleService.findAll().stream()
                .map(roleMapper::roleTeRoleResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponseDTO> findRoleById(@PathVariable Long roleId) {
        Role role = roleService.findRoleById(roleId);

        return ResponseEntity.ok(roleMapper.roleTeRoleResponse(role));
    }

    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@RequestBody RoleRequestDTO requestDTO,
                                                      HttpServletRequest request) {
        Role createdRole = roleService.createRole(roleMapper.roleRequestToRole(requestDTO));

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdRole.getId()))
                .body(roleMapper.roleTeRoleResponse(createdRole));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponseDTO> updateRoleById(@PathVariable Long roleId,
                                                          @RequestBody RoleRequestDTO requestDTO) {
        Role updatedRole = roleService.updateRoleById(roleId, roleMapper.roleRequestToRole(requestDTO));

        return ResponseEntity.ok(roleMapper.roleTeRoleResponse(updatedRole));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{roleId}")
    public void deleteRoleById(@PathVariable Long roleId) {
        roleService.deleteRoleById(roleId);
    }
}
