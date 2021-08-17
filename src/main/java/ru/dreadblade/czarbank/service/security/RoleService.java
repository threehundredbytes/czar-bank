package ru.dreadblade.czarbank.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.exception.RoleNameAlreadyExistsException;
import ru.dreadblade.czarbank.exception.RoleNotFoundException;
import ru.dreadblade.czarbank.repository.security.RoleRepository;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
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

        return roleRepository.save(role);
    }

    public Role updateRoleById(Long roleId, Role role) {
        Role roleToUpdate = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role doesn't exist"));

        if (roleRepository.existsByName(role.getName())) {
            throw new RoleNameAlreadyExistsException("Role with name \"" + role.getName() + "\" already exists");
        }

        roleToUpdate.setName(role.getName());

        return roleRepository.save(roleToUpdate);
    }

    public void deleteRoleById(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role doesn't exist");
        }

        roleRepository.deleteById(roleId);
    }
}
