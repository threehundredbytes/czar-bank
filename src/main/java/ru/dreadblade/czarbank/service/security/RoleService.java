package ru.dreadblade.czarbank.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.exception.*;
import ru.dreadblade.czarbank.repository.security.PermissionRepository;
import ru.dreadblade.czarbank.repository.security.RoleRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.ROLE_NOT_FOUND));
    }

    public Role findRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.ROLE_NOT_FOUND));
    }

    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new CzarBankException(ExceptionMessage.ROLE_NAME_ALREADY_EXISTS);
        }

        Set<Permission> existingPermissions = filterAndFindPermissionsFromDb(role.getPermissions());

        role.setPermissions(existingPermissions);

        return roleRepository.save(role);
    }

    public Role updateRoleById(Long roleId, Role role) {
        Role roleToUpdate = roleRepository.findById(roleId)
                .orElseThrow(() -> new CzarBankException(ExceptionMessage.ROLE_NOT_FOUND));

        String roleName = role.getName();
        Set<Permission> permissions = role.getPermissions();

        if (roleName != null && !roleName.isBlank()) {
            if (roleRepository.existsByName(roleName)) {
                throw new CzarBankException(ExceptionMessage.ROLE_NAME_ALREADY_EXISTS);
            }

            roleToUpdate.setName(roleName);
        }

        if (permissions != null && !permissions.isEmpty()) {
            Set<Permission> existingPermissions = filterAndFindPermissionsFromDb(permissions);

            roleToUpdate.setPermissions(existingPermissions);
        }

        return roleRepository.save(roleToUpdate);
    }

    public void deleteRoleById(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new CzarBankException(ExceptionMessage.ROLE_NOT_FOUND);
        }

        roleRepository.deleteById(roleId);
    }

    private Set<Permission> filterAndFindPermissionsFromDb(Set<Permission> permissions) {
        return permissions.stream()
                .filter(p -> permissionRepository.existsById(p.getId()))
                .map(p -> permissionRepository.findById(p.getId())
                        .orElseThrow(() -> new CzarBankException(ExceptionMessage.PERMISSION_NOT_FOUND)))
                .collect(Collectors.toSet());
    }
}
