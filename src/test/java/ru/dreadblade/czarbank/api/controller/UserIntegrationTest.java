package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.mapper.security.RoleMapper;
import ru.dreadblade.czarbank.api.mapper.security.UserMapper;
import ru.dreadblade.czarbank.api.model.request.security.PermissionRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.RoleRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.UserResponseDTO;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RoleRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("User Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserIntegrationTest extends BaseIntegrationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserMapper userMapper;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RoleMapper roleMapper;

    private static final String USERS_API_URL = "/api/users";

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            List<UserResponseDTO> expectedResponseDTOs = userRepository.findAll().stream()
                    .map(userMapper::userToUserResponseDTO)
                    .collect(Collectors.toList());

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTOs);

            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Transactional
        void findAll_isEmpty() throws Exception {
            userRepository.deleteAll();

            int expectedSize = 0;

            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)));
        }
    }

    @Nested
    @DisplayName("findUserById() Tests")
    class findUserByIdTests {
        @Test
        void findUserById_isSuccessful() throws Exception {
            User expectedUser = userRepository.findById(BASE_USER_ID + 1L).orElseThrow();

            String expectedResponse = objectMapper.writeValueAsString(userMapper.userToUserResponseDTO(expectedUser));

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        void findByUserId_isNotFound() throws Exception {
            long expectedUserId = BASE_USER_ID + 123L;

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUserId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("createUser() Tests")
    class createUserTests {
        @Test
        @Transactional
        void createUser_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();

            Assertions.assertThat(createdUser.getEmail()).isEqualTo(createdUser.getEmail());
            Assertions.assertThat(createdUser.getUserId()).isNotBlank();
        }

        @Test
        void createUser_userWithSameUsernameAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(BASE_USER_ID + 2L).orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username(existingUser.getUsername())
                    .email("user@czarbank.org")
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.USERNAME_ALREADY_EXISTS.getMessage()));
        }

        @Test
        void createUser_userWithSameEmailAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(BASE_USER_ID + 2L).orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("user")
                    .email(existingUser.getEmail())
                    .password("password")
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS.getMessage()));
        }
    }

    @Nested
    @DisplayName("updateUserById() Tests")
    class updateUserByIdTests {
        @Test
        @Transactional
        void updateUserById_isSuccessful() throws Exception {
            User userToBeUpdated = userRepository.findById(BASE_USER_ID + 4L).orElseThrow();

            Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .roles(Collections.singleton(RoleRequestDTO.builder()
                            .name(role.getName())
                            .permissions(role.getPermissions().stream()
                                    .map(p -> new PermissionRequestDTO(p.getId()))
                                    .collect(Collectors.toSet()))
                            .build()))
                    .build();

            UserResponseDTO responseAfterUpdate = userMapper.userToUserResponseDTO(userToBeUpdated);
            responseAfterUpdate.setUsername(requestDTO.getUsername());
            responseAfterUpdate.setEmail(requestDTO.getEmail());
            responseAfterUpdate.setRoles(requestDTO.getRoles().stream()
                    .map(r -> roleRepository.findByName(r.getName()).orElseThrow())
                    .map(roleMapper::roleTeRoleResponse)
                    .collect(Collectors.toSet()));

            String expectedResponse = objectMapper.writeValueAsString(responseAfterUpdate);

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));

            Assertions.assertThat(userToBeUpdated.getUsername()).isEqualTo(requestDTO.getUsername());
            Assertions.assertThat(userToBeUpdated.getEmail()).isEqualTo(requestDTO.getEmail());
        }

        @Test
        void updateUserById_isNotFound() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("updatingUser")
                    .email("updated@email.upd")
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + BASE_USER_ID + 123L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void updateUserById_userWithSameUsernameAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(BASE_USER_ID + 3L).orElseThrow();
            User userToBeUpdated = userRepository.findById(BASE_USER_ID + 4L).orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username(existingUser.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.USERNAME_ALREADY_EXISTS.getMessage()));
        }

        @Test
        void updateById_userWithSameEmailAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(BASE_USER_ID + 3L).orElseThrow();
            User userToBeUpdated = userRepository.findById(BASE_USER_ID + 4L).orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email(existingUser.getEmail())
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message")
                            .value(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS.getMessage()));
        }
    }

    @Nested
    @DisplayName("deleteUserById() Tests")
    class deleteUserByIdTests {
        @Test
        @Transactional
        void deleteUserById_isSuccessful() throws Exception {
            long userDeletionId = BASE_USER_ID + 4L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isTrue();

            Set<Role> roles = userRepository.findById(userDeletionId).orElseThrow().getRoles();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();
            Assertions.assertThat(roleRepository.findAll()).containsAll(roles);
        }

        @Test
        void deleteUserById_isNotFound() throws Exception {
            long userDeletionId = BASE_USER_ID + 123L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNotFound());
        }
    }
}
