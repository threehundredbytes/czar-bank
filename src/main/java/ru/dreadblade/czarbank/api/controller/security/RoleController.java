package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.security.RoleMapper;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.api.model.request.validation.CreateRequest;
import ru.dreadblade.czarbank.api.model.request.validation.UpdateRequest;
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

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping
    public ResponseEntity<List<RoleResponseDTO>> findAll() {
        return ResponseEntity.ok(roleService.findAll().stream()
                .map(roleMapper::entityToResponseDto)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('ROLE_READ')")
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponseDTO> findRoleById(@PathVariable Long roleId) {
        Role role = roleService.findRoleById(roleId);

        return ResponseEntity.ok(roleMapper.entityToResponseDto(role));
    }

    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@Validated(CreateRequest.class) @RequestBody RoleRequestDTO requestDTO,
                                                      HttpServletRequest request) {
        Role createdRole = roleService.createRole(roleMapper.requestDtoToEntity(requestDTO));

        return ResponseEntity.created(URI.create(request.getRequestURI() + "/" + createdRole.getId()))
                .body(roleMapper.entityToResponseDto(createdRole));
    }

    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponseDTO> updateRoleById(@PathVariable Long roleId,
                                                          @Validated(UpdateRequest.class) @RequestBody RoleRequestDTO requestDTO) {
        Role updatedRole = roleService.updateRoleById(roleId, roleMapper.requestDtoToEntity(requestDTO));

        return ResponseEntity.ok(roleMapper.entityToResponseDto(updatedRole));
    }

    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{roleId}")
    public void deleteRoleById(@PathVariable Long roleId) {
        roleService.deleteRoleById(roleId);
    }
}
