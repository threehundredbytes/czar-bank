package ru.dreadblade.czarbank.repository.security;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dreadblade.czarbank.domain.security.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
