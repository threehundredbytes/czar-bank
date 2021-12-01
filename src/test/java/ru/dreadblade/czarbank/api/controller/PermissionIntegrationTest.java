package ru.dreadblade.czarbank.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.mapper.security.PermissionMapper;
import ru.dreadblade.czarbank.api.model.response.security.PermissionResponseDTO;
import ru.dreadblade.czarbank.repository.security.PermissionRepository;
import ru.dreadblade.czarbank.repository.security.RoleRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Permission Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PermissionIntegrationTest extends BaseIntegrationTest  {
    @Autowired
    PermissionRepository permissionRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PermissionMapper permissionMapper;

    private final static String PERMISSIONS_API_URL = "/api/permissions";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {
        @Test
        @WithUserDetails("admin")
        void findAll_withAuth_withPermission_isSuccessful() throws Exception {
            List<PermissionResponseDTO> expectedPermissions = permissionRepository.findAll().stream()
                    .map(permissionMapper::permissionToPermissionResponse)
                    .collect(Collectors.toList());

            long expectedSize = permissionRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(expectedPermissions);

            mockMvc.perform(get(PERMISSIONS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findAll_withAuth_isFailed() throws Exception {
            mockMvc.perform(get(PERMISSIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findAll_withoutAuth_isFailed() throws Exception {
            mockMvc.perform(get(PERMISSIONS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        @Transactional
        void findAll_withAuth_withPermission_isEmpty() throws Exception {
            roleRepository.findAll().forEach(role -> role.setPermissions(Collections.emptySet()));

            permissionRepository.deleteAll();

            long expectedSize = permissionRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(Collections.emptySet());

            mockMvc.perform(get(PERMISSIONS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }
    }
}
