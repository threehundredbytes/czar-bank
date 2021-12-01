package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.model.request.security.PermissionRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.domain.security.Permission;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.PermissionRepository;
import ru.dreadblade.czarbank.repository.security.RoleRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Role Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RoleIntegrationTest extends BaseIntegrationTest {
    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionRepository permissionRepository;

    private static final String ROLES_API_URL = "/api/roles";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {
        @Test
        @WithUserDetails("admin")
        void findAll_withAuth_withPermission_isSuccessful() throws Exception {
            List<Role> roles = roleRepository.findAll();

            long expectedSize = roleRepository.count();

            String expectedResponse = objectMapper.writeValueAsString(roles);

            mockMvc.perform(get(ROLES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findAll_withAuth_isFailed() throws Exception {
            mockMvc.perform(get(ROLES_API_URL))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findAll_withoutAuth_isFailed() throws Exception {
            mockMvc.perform(get(ROLES_API_URL))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        @Transactional
        void findAll_withAuth_withPermission_isEmpty() throws Exception {
            roleRepository.findAll().forEach(role -> role.setPermissions(Collections.emptySet()));
            userRepository.findAll().forEach(user -> user.setRoles(Collections.emptySet()));

            roleRepository.deleteAll();

            long expectedSize = 0L;

            mockMvc.perform(get(ROLES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))));
        }
    }

    @Nested
    @DisplayName("findRoleById() Tests")
    class FindRoleByIdTests {
        @Test
        @WithUserDetails("admin")
        void findRoleById_withAuth_withPermission_isSuccessful() throws Exception {
            Role expectedRole = roleRepository.findById(BASE_ROLE_ID + 1L).orElseThrow();

            String expectedResponse = objectMapper.writeValueAsString(expectedRole);

            mockMvc.perform(get(ROLES_API_URL + "/" + expectedRole.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findRoleById_withAuth_isFailed() throws Exception {
            long expectedRoleId =BASE_ROLE_ID + 1L;

            mockMvc.perform(get(ROLES_API_URL + "/" + expectedRoleId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findRoleById_withoutAuth_isFailed() throws Exception {
            long expectedRoleId =BASE_ROLE_ID + 1L;

            mockMvc.perform(get(ROLES_API_URL + "/" + expectedRoleId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void findRoleById_withAuth_withPermission_isNotFound() throws Exception {
            long expectedRoleId = BASE_ROLE_ID + 123L;

            Assertions.assertThat(roleRepository.existsById(expectedRoleId)).isFalse();

            mockMvc.perform(get(ROLES_API_URL + "/" + expectedRoleId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.ROLE_NOT_FOUND.getMessage()));
        }
    }

    @Nested
    @DisplayName("createRole() Tests")
    class CreateRole {
        @Test
        @WithUserDetails("admin")
        @Transactional
        void createRole_withAuth_withPermission_isSuccessful() throws Exception {
            Set<PermissionRequestDTO> permissions = new HashSet<>();
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 2L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build();

            Set<Permission> expectedPermissions = permissions.stream()
                    .filter(dto -> permissionRepository.existsById(dto.getId()))
                    .map(dto -> permissionRepository.findById(dto.getId()).orElseThrow())
                    .collect(Collectors.toSet());

            mockMvc.perform(post(ROLES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists(HttpHeaders.LOCATION))
                    .andExpect(header().string(HttpHeaders.LOCATION, containsString(ROLES_API_URL)))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value(requestDTO.getName()))
                    .andExpect(jsonPath("$.permissions", hasSize(permissions.size())));

            Role createdRole = roleRepository.findByName(requestDTO.getName()).orElseThrow();

            Assertions.assertThat(createdRole.getName()).isEqualTo(requestDTO.getName());
            Assertions.assertThat(createdRole.getPermissions()).hasSize(permissions.size());
            Assertions.assertThat(createdRole.getPermissions()).containsExactlyInAnyOrderElementsOf(expectedPermissions);
        }

        @Test
        @WithUserDetails("client")
        void createRole_withAuth_isFailed() throws Exception {
            Set<PermissionRequestDTO> permissions = new HashSet<>();
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 2L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build();

            mockMvc.perform(post(ROLES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsByName(requestDTO.getName())).isFalse();
        }

        @Test
        void createRole_withoutAuth_isFailed() throws Exception {
            Set<PermissionRequestDTO> permissions = new HashSet<>();
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 2L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            permissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build();

            mockMvc.perform(post(ROLES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsByName(requestDTO.getName())).isFalse();
        }

        @Test
        @WithUserDetails("admin")
        void createRole_withAuth_withPermission_roleWithSameNameAlreadyExists() throws Exception {
            Role existingRole = roleRepository.findById(BASE_ROLE_ID + 1L).orElseThrow();

            long rolesCountBeforeCreating = roleRepository.count();

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name(existingRole.getName())
                    .build();

            mockMvc.perform(post(ROLES_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.ROLE_NAME_ALREADY_EXISTS.getMessage()));

            Assertions.assertThat(rolesCountBeforeCreating).isEqualTo(roleRepository.count());
        }
    }

    @Nested
    @DisplayName("updateRole() Tests")
    class UpdateRoleById {
        @Test
        @WithUserDetails("admin")
        @Transactional
        void updateRole_withAuth_withPermission_isSuccessful() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            String roleName = "MANAGER";

            Role roleToUpdate = roleRepository.save(Role.builder()
                    .name(roleName)
                    .permissions(permissions)
                    .build());

            Set<PermissionRequestDTO> updatedPermissions = new HashSet<>();
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 4L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 5L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 8L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            Set<Permission> expectedPermissions = updatedPermissions.stream()
                    .filter(dto -> permissionRepository.existsById(dto.getId()))
                    .map(dto -> permissionRepository.findById(dto.getId()).orElseThrow())
                    .collect(Collectors.toSet());

            int expectedSize = updatedPermissions.size();

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("upd" + roleToUpdate.getName())
                    .permissions(updatedPermissions)
                    .build();

            mockMvc.perform(put(ROLES_API_URL + "/" + roleToUpdate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.name").value(requestDTO.getName()))
                    .andExpect(jsonPath("$.permissions", hasSize(expectedSize)));

            Assertions.assertThat(roleRepository.existsByName(roleName)).isFalse();

            Assertions.assertThat(roleToUpdate.getName()).isEqualTo(requestDTO.getName());
            Assertions.assertThat(roleToUpdate.getPermissions()).containsExactlyInAnyOrderElementsOf(expectedPermissions);
        }

        @Test
        @WithUserDetails("client")
        @Rollback
        void updateRole_withAuth_isFailed() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            Role roleToUpdate = roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build());

            Set<PermissionRequestDTO> updatedPermissions = new HashSet<>();
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 4L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 5L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 8L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("upd" + roleToUpdate.getName())
                    .permissions(updatedPermissions)
                    .build();

            mockMvc.perform(put(ROLES_API_URL + "/" + roleToUpdate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsByName(roleToUpdate.getName())).isTrue();
        }

        @Test
        @Rollback
        void updateRole_withoutAuth_isFailed() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            Role roleToUpdate = roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build());

            Set<PermissionRequestDTO> updatedPermissions = new HashSet<>();
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 4L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 5L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 6L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 7L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 8L).build());
            updatedPermissions.add(PermissionRequestDTO.builder().id(BASE_PERMISSION_ID + 10L).build());

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("upd" + roleToUpdate.getName())
                    .permissions(updatedPermissions)
                    .build();

            mockMvc.perform(put(ROLES_API_URL + "/" + roleToUpdate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsByName(roleToUpdate.getName())).isTrue();
        }

        @Test
        @WithUserDetails("admin")
        void updateRole_withAuth_withPermission_isNotFound() throws Exception {
            long roleToUpdateId = BASE_ROLE_ID + 123;

            Assertions.assertThat(roleRepository.existsById(roleToUpdateId)).isFalse();

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name("upd")
                    .build();

            mockMvc.perform(put(ROLES_API_URL + "/" + roleToUpdateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.ROLE_NOT_FOUND.getMessage()));

        }

        @Test
        @WithUserDetails("admin")
        void updateRole_withAuth_withPermission_roleWithSameNameAlreadyExists() throws Exception {
            Role existingRole = roleRepository.findById(BASE_ROLE_ID + 1L).orElseThrow();
            Role roleToUpdate = roleRepository.findById(BASE_ROLE_ID + 2L).orElseThrow();

            RoleRequestDTO requestDTO = RoleRequestDTO.builder()
                    .name(existingRole.getName())
                    .build();

            mockMvc.perform(put(ROLES_API_URL + "/" + roleToUpdate.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.ROLE_NAME_ALREADY_EXISTS.getMessage()));
        }
    }

    @Nested
    @DisplayName("deleteRoleById() Tests")
    class DeleteRole {
        @Test
        @WithUserDetails("admin")
        @Rollback
        void deleteRole_withAuth_withPermission_isSuccessful() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            Role roleToDelete = roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build());

            int permissionsCountBeforeDelete = permissions.size();

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isTrue();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isTrue();

            mockMvc.perform(delete(ROLES_API_URL + "/" + roleToDelete.getId()))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isFalse();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isFalse();

            Set<Permission> existingPermissions = permissions.stream()
                    .filter(permission -> permissionRepository.existsById(permission.getId()))
                    .collect(Collectors.toSet());

            Assertions.assertThat(permissionsCountBeforeDelete).isEqualTo(existingPermissions.size());
            Assertions.assertThat(existingPermissions).containsExactlyInAnyOrderElementsOf(permissions);
        }

        @Test
        @WithUserDetails("client")
        @Rollback
        void deleteRole_withAuth_isFailed() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            Role roleToDelete = roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build());

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isTrue();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isTrue();

            mockMvc.perform(delete(ROLES_API_URL + "/" + roleToDelete.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isTrue();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isTrue();
        }

        @Test
        @Rollback
        void deleteRole_withoutAuth_isFailed() throws Exception {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 2L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 6L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 7L).orElseThrow());
            permissions.add(permissionRepository.findById(BASE_PERMISSION_ID + 10L).orElseThrow());

            Role roleToDelete = roleRepository.save(Role.builder()
                    .name("MANAGER")
                    .permissions(permissions)
                    .build());

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isTrue();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isTrue();

            mockMvc.perform(delete(ROLES_API_URL + "/" + roleToDelete.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(roleRepository.existsById(roleToDelete.getId())).isTrue();
            Assertions.assertThat(roleRepository.existsByName(roleToDelete.getName())).isTrue();
        }

        @Test
        @WithUserDetails("admin")
        void deleteRole_withAuth_withPermission_isNotFound() throws Exception {
            long userToDeleteId = BASE_USER_ID + 123L;

            Assertions.assertThat(roleRepository.existsById(userToDeleteId)).isFalse();

            mockMvc.perform(delete(ROLES_API_URL + "/" + userToDeleteId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(ExceptionMessage.ROLE_NOT_FOUND.getMessage()));
        }
    }
}
