package ru.dreadblade.czarbank.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.exception.PermissionNotFoundException;
import ru.dreadblade.czarbank.exception.RoleNameAlreadyExistsException;
import ru.dreadblade.czarbank.exception.RoleNotFoundException;
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
                .orElseThrow(() -> new RoleNotFoundException("Role doesn't exist"));
    }

    public Role createRole(Role role) {
        if (roleRepository.existsByName(role.getName())) {
            throw new RoleNameAlreadyExistsException("Role with name \"" + role.getName() + "\" already exists");
        }

        Set<Permission> existingPermissions = filterAndFindPermissionsFromDb(role.getPermissions());

        role.setPermissions(existingPermissions);

        return roleRepository.save(role);
    }

    public Role updateRoleById(Long roleId, Role role) {
        Role roleToUpdate = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role doesn't exist"));

        if (roleRepository.existsByName(role.getName())) {
            throw new RoleNameAlreadyExistsException("Role with name \"" + role.getName() + "\" already exists");
        }

        roleToUpdate.setName(role.getName());

        Set<Permission> existingPermissions = filterAndFindPermissionsFromDb(role.getPermissions());

        roleToUpdate.setPermissions(existingPermissions);

        return roleRepository.save(roleToUpdate);
    }

    public void deleteRoleById(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role doesn't exist");
        }

        roleRepository.deleteById(roleId);
    }

    // filters only existing permissions
    // get values

    private Set<Permission> filterAndFindPermissionsFromDb(Set<Permission> permissions) {
        return permissions.stream()
                .filter(p -> permissionRepository.existsById(p.getId()))
                .map(p -> permissionRepository.findById(p.getId())
                        .orElseThrow(() -> new PermissionNotFoundException("Permission doesn't exist")))
                .collect(Collectors.toSet());
    }
}
