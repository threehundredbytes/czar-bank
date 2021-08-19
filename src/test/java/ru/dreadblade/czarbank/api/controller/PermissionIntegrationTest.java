package ru.dreadblade.czarbank.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.repository.security.PermissionRepository;
import ru.dreadblade.czarbank.repository.security.RoleRepository;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("Permission Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PermissionIntegrationTest extends BaseIntegrationTest  {
    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    RoleRepository roleRepository;

    private final static String PERMISSIONS_API_URL = "/api/permissions";

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            List<Permission> expectedPermissions = permissionRepository.findAll();

            long expectedSize = permissionRepository.count();

            Permission expectedPermission1 = expectedPermissions.get(0);
            Permission expectedPermission6 = expectedPermissions.get(5);
            Permission expectedPermission12 = expectedPermissions.get(11);

            mockMvc.perform(get(PERMISSIONS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(jsonPath("$[0].name").value(expectedPermission1.getName()))
                    .andExpect(jsonPath("$[5].name").value(expectedPermission6.getName()))
                    .andExpect(jsonPath("$[11].name").value(expectedPermission12.getName()));
        }

        @Test
        @Transactional
        void findAll_isEmpty() throws Exception {
            roleRepository.findAll().forEach(role -> role.setPermissions(Collections.emptySet()));

            permissionRepository.deleteAll();

            long expectedSize = 0;

            mockMvc.perform(get(PERMISSIONS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))));
        }
    }
}
