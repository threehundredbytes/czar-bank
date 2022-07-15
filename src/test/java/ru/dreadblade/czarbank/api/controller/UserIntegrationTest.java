package ru.dreadblade.czarbank.api.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.dreadblade.czarbank.api.mapper.security.RoleMapper;
import ru.dreadblade.czarbank.api.mapper.security.UserMapper;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.UserResponseDTO;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RoleRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("smtp")
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

    @MockBean
    MailSender mailSender;

    private static final String USERS_API_URL = "/api/users";

    @Nested
    @DisplayName("findAll() Tests")
    class FindAllTests {
        @Test
        @WithUserDetails("admin")
        void findAll_withAuth_withPermission_isSuccessful() throws Exception {
            List<UserResponseDTO> expectedResponseDTOs = userRepository.findAll().stream()
                    .map(userMapper::entityToResponseDto)
                    .collect(Collectors.toList());

            String expectedResponse = objectMapper.writeValueAsString(expectedResponseDTOs);

            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findAll_withAuth_isFailed() throws Exception {
            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findAll_withoutAuth_isFailed() throws Exception {
            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        @Rollback
        void findAll_withAuth_withPermission_isEmpty() throws Exception {
            userRepository.deleteAll();

            int expectedSize = 0;

            mockMvc.perform(get(USERS_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(expectedSize)));
        }
    }

    @Nested
    @DisplayName("findUserById() Tests")
    class FindUserByIdTests {
        @Test
        @WithUserDetails("admin")
        void findUserById_withAuth_withPermission_isSuccessful() throws Exception {
            User expectedUser = userRepository.findById(2L).orElseThrow();

            String expectedResponse = objectMapper.writeValueAsString(userMapper.entityToResponseDto(expectedUser));

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findUserById_withAuth_asSelf_isSuccessful() throws Exception {
            User expectedUser = userRepository.findById(3L).orElseThrow();

            String expectedResponse = objectMapper.writeValueAsString(userMapper.entityToResponseDto(expectedUser));

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @WithUserDetails("client")
        void findUserById_withAuth_isFailed() throws Exception {
            User expectedUser = userRepository.findById(1L).orElseThrow();

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        void findUserById_withoutAuth_isFailed() throws Exception {
            User expectedUser = userRepository.findById(1L).orElseThrow();

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUser.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void findByUserId_withAuth_withPermission_isNotFound() throws Exception {
            long expectedUserId = 123L;

            mockMvc.perform(get(USERS_API_URL + "/" + expectedUserId))
                    .andExpect(status().isNotFound());
        }

        @Nested
        @DisplayName("Request validation tests")
        class RequestValidationTests {
            @Test
            @WithUserDetails("admin")
            void findUserById_withAuth_withPermission_invalidUserId_isFailed_responseIsValid() throws Exception {
                String expectedUserId = "userIdMustBeLong!";

                String requestUrl = USERS_API_URL + "/" + expectedUserId;

                mockMvc.perform(get(requestUrl))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                        .andExpect(jsonPath("$.error").value(HttpStatus.BAD_REQUEST.getReasonPhrase()))
                        .andExpect(jsonPath("$.message").value("The parameter «userId» with value of «"
                                + expectedUserId + "» cannot be converted to «Long»"))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }
        }
    }

    @Nested
    @DisplayName("createUser() Tests")
    class CreateUserTests {
        @Test
        @WithUserDetails("admin")
        @Transactional
        void createUser_withAuth_withPermission_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .addRole(2L)
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();

            Assertions.assertThat(createdUser.getEmail()).isEqualTo(createdUser.getEmail());
            Assertions.assertThat(createdUser.getRoles()).contains(roleRepository.findByName("EMPLOYEE").orElseThrow());
        }

        @Test
        @Transactional
        void createUser_withoutAuth_isSuccessful() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("boyarin")
                    .email("boyarin@czarbank.org")
                    .password("c0mp1exP@ssw0rd")
                    .addRole(2L)
                    .build();

            mockMvc.perform(post(USERS_API_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());

            Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(SimpleMailMessage.class));

            User createdUser = userRepository.findByUsername(requestDTO.getUsername()).orElseThrow();

            Assertions.assertThat(createdUser.getEmail()).isEqualTo(createdUser.getEmail());
            Assertions.assertThat(createdUser.getUserId()).isNotBlank();
            Assertions.assertThat(createdUser.getRoles()).doesNotContain(roleRepository.findByName("EMPLOYEE").orElseThrow());
        }

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {
            @Test
            void createUser_withoutAuth_withExistingUsername_validationIsFailed_responseIsCorrect() throws Exception {
                User existingUser = userRepository.findById(2L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username(existingUser.getUsername())
                        .email("user@czarbank.org")
                        .password("c0mp1exP@ssw0rd")
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("User with the same username already exists"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            void createUser_withoutAuth_withExistingEmail_validationIsFailed_responseIsCorrect() throws Exception {
                User existingUser = userRepository.findById(2L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("user")
                        .email(existingUser.getEmail())
                        .password("c0mp1exP@ssw0rd")
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("email"))
                        .andExpect(jsonPath("$.errors[0].message").value("User with the same email already exists"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withShortUsername_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username(RandomStringUtils.randomAlphabetic(2))
                        .email("boyarin@czarbank.org")
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("The username must be between 3 and 32 characters long (inclusive)"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withLongUsername_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username(RandomStringUtils.randomAlphabetic(33))
                        .email("boyarin@czarbank.org")
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("The username must be between 3 and 32 characters long (inclusive)"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withoutUsername_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .email("boyarin@czarbank.org")
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("Username must be not empty"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withShortInvalidEmail_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email(RandomStringUtils.randomAlphabetic(2))
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email", "email")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Invalid email address", "The email must be between 3 and 254 characters long (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withLongInvalidEmail_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email(RandomStringUtils.randomAlphabetic(255))
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email", "email")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Invalid email address", "The email must be between 3 and 254 characters long (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withInvalidEmail_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email("boyarin@czarbank.organization")
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[*].field").value( "email"))
                        .andExpect(jsonPath("$.errors[*].message").value("Invalid email address"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withInvalidPassword_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email("boyarin@czarbank.org")
                        .password("easypassword")
                        .addRole(2L)
                        .build();

                String invalidPasswordMessage = "The password must contain at least 8 characters, contain at least 1 number, " +
                "1 lowercase and 1 uppercase letter, and a special character (!~<>,;:_=?*+#.\"'&§%°()|[]-$^@/)";

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("password"))
                        .andExpect(jsonPath("$.errors[0].message").value(invalidPasswordMessage))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withoutEmail_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .password("c0mp1exP@ssw0rd")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("email"))
                        .andExpect(jsonPath("$.errors[0].message").value("Email must be not empty"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withoutPassword_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email("boyarin@czarbank.org")
                        .addRole(2L)
                        .build();

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("password"))
                        .andExpect(jsonPath("$.errors[0].message").value("Password must be not empty"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }

            @Test
            @WithUserDetails("admin")
            void createUser_withAuth_withPermission_withoutUsername_withoutEmail_withoutPassword_validationIsFailed_responseIsCorrect() throws Exception {
                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .addRole(2L)
                        .build();

                String[] validationFields = { "username", "email", "password" };
                String[] validationMessages = { "Username must be not empty", "Email must be not empty",
                        "Password must be not empty" };

                mockMvc.perform(post(USERS_API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andDo(print())
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(3)))
                        .andExpect(jsonPath("$.errors[*].field")
                                .value(containsInAnyOrder(validationFields)))
                        .andExpect(jsonPath("$.errors[*].message")
                                .value(containsInAnyOrder(validationMessages)))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(USERS_API_URL));
            }
        }
    }

    @Nested
    @DisplayName("updateUserById() Tests")
    class UpdateUserByIdTests {
        @Test
        @Transactional
        @WithUserDetails("admin")
        void updateUserById_withAuth_withPermission_isSuccessful() throws Exception {
            User userToBeUpdated = userRepository.findById(4L).orElseThrow();

            Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .addRole(role.getId())
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(userToBeUpdated.getId()))
                    .andExpect(jsonPath("$.username").value(requestDTO.getUsername()))
                    .andExpect(jsonPath("$.email").value(requestDTO.getEmail()))
                    .andExpect(jsonPath("$.roles", hasSize(1)))
                    .andExpect(jsonPath("$.roles[0].id").value(role.getId()))
                    .andExpect(jsonPath("$.roles[0].name").value(role.getName()))
                    .andExpect(jsonPath("$.roles[0].permissions", hasSize(role.getPermissions().size())));

            Assertions.assertThat(userToBeUpdated.getUsername()).isEqualTo(requestDTO.getUsername());
            Assertions.assertThat(userToBeUpdated.getEmail()).isEqualTo(requestDTO.getEmail());
            Assertions.assertThat(userToBeUpdated.getRoles()).containsExactly(role);
        }

        @Test
        @Transactional
        @WithUserDetails("client")
        void updateUserById_withAuth_asSelf_isSuccessful() throws Exception {
            User userToBeUpdated = userRepository.findById(3L).orElseThrow();

            Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .addRole(role.getId())
                    .build();

            UserResponseDTO responseAfterUpdate = userMapper.entityToResponseDto(userToBeUpdated);
            responseAfterUpdate.setUsername(requestDTO.getUsername());
            responseAfterUpdate.setEmail(requestDTO.getEmail());
            responseAfterUpdate.setRoles(userToBeUpdated.getRoles().stream()
                    .map(roleMapper::entityToResponseDto)
                    .collect(Collectors.toSet()));

            String expectedResponse = objectMapper.writeValueAsString(responseAfterUpdate);

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(expectedResponse));

            Assertions.assertThat(userToBeUpdated.getUsername()).isEqualTo(requestDTO.getUsername());
            Assertions.assertThat(userToBeUpdated.getEmail()).isEqualTo(requestDTO.getEmail());
            Assertions.assertThat(userToBeUpdated.getRoles()).doesNotContain(role);
        }

        @Test
        @WithUserDetails("client")
        void updateUserById_withAuth_isFailed() throws Exception {
            User userToBeUpdated = userRepository.findById(4L).orElseThrow();

            Role role = roleRepository.findByName("EMPLOYEE").orElseThrow();

            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("upd" + userToBeUpdated.getUsername())
                    .email("upd" + userToBeUpdated.getEmail())
                    .addRole(role.getId())
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + userToBeUpdated.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));
        }

        @Test
        @WithUserDetails("admin")
        void updateUserById_withAuth_withPermission_isNotFound() throws Exception {
            UserRequestDTO requestDTO = UserRequestDTO.builder()
                    .username("updatingUser")
                    .email("updated@email.upd")
                    .build();

            mockMvc.perform(put(USERS_API_URL + "/" + 123L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithUserDetails("admin")
        void updateUserById_withAuth_withPermission_userWithSameUsernameAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(3L).orElseThrow();
            User userToBeUpdated = userRepository.findById(4L).orElseThrow();

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
        @WithUserDetails("admin")
        void updateById_withAuth_withPermission_userWithSameEmailAlreadyExists() throws Exception {
            User existingUser = userRepository.findById(3L).orElseThrow();
            User userToBeUpdated = userRepository.findById(4L).orElseThrow();

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

        @Nested
        @DisplayName("Validation Tests")
        class ValidationTests {
            @Test
            @WithUserDetails("admin")
            void updateUserById_withAuth_withPermission_withShortUsername_withoutEmail_withoutRoles_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username(RandomStringUtils.randomAlphabetic(2))
                        .build();

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("The username must be between 3 and 32 characters long (inclusive)"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }

            @Test
            @WithUserDetails("admin")
            void updateUserById_withAuth_withPermission_withLongUsername_withoutEmail_withoutRoles_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username(RandomStringUtils.randomAlphabetic(33))
                        .build();

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("username"))
                        .andExpect(jsonPath("$.errors[0].message").value("The username must be between 3 and 32 characters long (inclusive)"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }

            @Test
            @WithUserDetails("admin")
            void updateUserById_withAuth_withPermission_withoutUsername_withShortInvalidEmail_withoutRoles_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .email(RandomStringUtils.randomAlphabetic(2))
                        .build();

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email", "email")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Invalid email address", "The email must be between 3 and 254 characters long (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }

            @Test
            @WithUserDetails("admin")
            void updateUserById_withAuth_withPermission_withoutUsername_withLongInvalidEmail_withoutRoles_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .email(RandomStringUtils.randomAlphabetic(255))
                        .build();

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(2)))
                        .andExpect(jsonPath("$.errors[*].field").value(containsInAnyOrder("email", "email")))
                        .andExpect(jsonPath("$.errors[*].message").value(containsInAnyOrder("Invalid email address", "The email must be between 3 and 254 characters long (inclusive)")))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }

            @Test
            @WithUserDetails("admin")
            void updateUser_withAuth_withPermission_withInvalidPassword_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .username("boyarin")
                        .email("boyarin@czarbank.org")
                        .password("easypassword")
                        .build();

                String invalidPasswordMessage = "The password must contain at least 8 characters, contain at least 1 number, " +
                        "1 lowercase and 1 uppercase letter, and a special character (!~<>,;:_=?*+#.\"'&§%°()|[]-$^@/)";

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("password"))
                        .andExpect(jsonPath("$.errors[0].message").value(invalidPasswordMessage))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }
            @Test
            @WithUserDetails("admin")
            void updateUserById_withAuth_withPermission_withoutUsername_withoutEmail_withNullRoles_validationIsFailed_responseIsCorrect() throws Exception {
                User userToBeUpdated = userRepository.findById(4L).orElseThrow();

                UserRequestDTO requestDTO = UserRequestDTO.builder()
                        .build();

                requestDTO.setRoles(null);

                String requestUrl = USERS_API_URL + "/" + userToBeUpdated.getId();

                mockMvc.perform(put(requestUrl)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
                        .andExpect(status().isUnprocessableEntity())
                        .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                        .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                        .andExpect(jsonPath("$.error").value(VALIDATION_ERROR))
                        .andExpect(jsonPath("$.errors", hasSize(1)))
                        .andExpect(jsonPath("$.errors[0].field").value("roles"))
                        .andExpect(jsonPath("$.errors[0].message").value("User roles cannot be null"))
                        .andExpect(jsonPath("$.message").value(INVALID_REQUEST))
                        .andExpect(jsonPath("$.path").value(requestUrl));
            }
        }
    }

    @Nested
    @DisplayName("deleteUserById() Tests")
    class DeleteUserByIdTests {
        @Test
        @Transactional
        @WithUserDetails("admin")
        void deleteUserById_withAuth_withPermission_isSuccessful() throws Exception {
            long userDeletionId = 4L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isTrue();

            Set<Role> roles = userRepository.findById(userDeletionId).orElseThrow().getRoles();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();
            Assertions.assertThat(roleRepository.findAll()).containsAll(roles);
        }

        @Test
        @Transactional
        @WithUserDetails("client")
        void deleteUserById_withAuth_asSelf_isSuccessful() throws Exception {
            long userDeletionId = 3L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isTrue();

            Set<Role> roles = userRepository.findById(userDeletionId).orElseThrow().getRoles();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNoContent());

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();
            Assertions.assertThat(roleRepository.findAll()).containsAll(roles);
        }

        @Test
        @WithUserDetails("client")
        void deleteUserById_withAuth_isFailed() throws Exception {
            long userDeletionId = 4L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isTrue();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access is denied"));

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isTrue();
        }

        @Test
        @WithUserDetails("admin")
        void deleteUserById_withAuth_withPermission_isNotFound() throws Exception {
            long userDeletionId = 123L;

            Assertions.assertThat(userRepository.existsById(userDeletionId)).isFalse();

            mockMvc.perform(delete(USERS_API_URL + "/" + userDeletionId))
                    .andExpect(status().isNotFound());
        }
    }
}
