package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.security.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}
