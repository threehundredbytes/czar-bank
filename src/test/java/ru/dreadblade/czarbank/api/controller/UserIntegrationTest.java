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
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DisplayName("User Integration Tests")
@Sql(value = "/user/users-insertion.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/user/users-deletion.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserIntegrationTest extends BaseIntegrationTest {
    @Autowired
    UserRepository userRepository;

    private static final String USERS_API_URL = "/api/users";
    private static final String USER_WITH_SAME_USERNAME_ALREADY_EXISTS_MESSAGE = "User with username \"%s\" already exists";
    private static final String USER_WITH_SAME_EMAIL_ALREADY_EXISTS_MESSAGE = "User with email \"%s\" already exists";

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            List<User> usersFromDb = userRepository.findAll();

            long expectedSize = userRepository.count();

            User expectedUser1 = usersFromDb.get(0);
            User expectedUser2 = usersFromDb.get(1);
            User expectedUser3 = usersFromDb.get(2);

            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(jsonPath("$[0].userId").value(expectedUser1.getUserId()))
                    .andExpect(jsonPath("$[0].username").value(expectedUser1.getUsername()))
                    .andExpect(jsonPath("$[0].email").value(expectedUser1.getEmail()))
                    .andExpect(jsonPath("$[1].userId").value(expectedUser2.getUserId()))
                    .andExpect(jsonPath("$[1].username").value(expectedUser2.getUsername()))
                    .andExpect(jsonPath("$[1].email").value(expectedUser2.getEmail()))
                    .andExpect(jsonPath("$[2].userId").value(expectedUser3.getUserId()))
                    .andExpect(jsonPath("$[2].username").value(expectedUser3.getUsername()))
                    .andExpect(jsonPath("$[2].email").value(expectedUser3.getEmail()));
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

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(jsonPath("$.id").value(expectedUser.getId()))
                    .andExpect(jsonPath("$.userId").value(expectedUser.getUserId()))
                    .andExpect(jsonPath("$.username").value(expectedUser.getUsername()))
                    .andExpect(jsonPath("$.email").value(expectedUser.getEmail()));
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
                    .andExpect(jsonPath("$.message").value(String
                            .format(USER_WITH_SAME_USERNAME_ALREADY_EXISTS_MESSAGE, requestDTO.getUsername())));
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
                    .andExpect(jsonPath("$.message").value(String
                            .format(USER_WITH_SAME_EMAIL_ALREADY_EXISTS_MESSAGE, requestDTO.getEmail())));
        }
    }

    @Nested
    @DisplayName("updateUserById() Tests")
    class updateUserByIdTests {
        @Test
        @Transactional
        void updateUserById_isSuccessful() throws Exception {
            User userToBeUpdated = userRepository.findById(BASE_USER_ID + 4L).orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userToBeUpdated.getId()))
                    .andExpect(jsonPath("$.username").value(requestDTO.getUsername()))
                    .andExpect(jsonPath("$.email").value(requestDTO.getEmail()));

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
                    .andExpect(jsonPath("$.message").value(String
                            .format(USER_WITH_SAME_USERNAME_ALREADY_EXISTS_MESSAGE, requestDTO.getUsername())));
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
                    .andExpect(jsonPath("$.message").value(String
                            .format(USER_WITH_SAME_EMAIL_ALREADY_EXISTS_MESSAGE, requestDTO.getEmail())));
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

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();
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
