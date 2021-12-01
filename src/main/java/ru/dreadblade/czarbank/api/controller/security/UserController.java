package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.mapper.security.UserMapper;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.UserResponseDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.service.security.UserService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/api/users")
@RestController
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @Autowired
    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll().stream()
                .map(userMapper::userToUserResponseDTO)
                .collect(Collectors.toList()));
    }

    @PreAuthorize("hasAuthority('USER_READ') or (isAuthenticated() and #userId == principal.id)")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> findUserById(@PathVariable Long userId) {
        User user = userService.findUserById(userId);

        return ResponseEntity.ok(userMapper.userToUserResponseDTO(user));
    }

    @PreAuthorize("hasAuthority('USER_CREATE') or !isAuthenticated()")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO requestDTO,
                                                      HttpServletRequest request,
                                                      @AuthenticationPrincipal User currentUser) {
        User createdUser = userService.createUser(userMapper.userRequestToUser(requestDTO), currentUser);

        return ResponseEntity.created(URI.create(request.getRequestURI() + createdUser.getId()))
                .body(userMapper.userToUserResponseDTO(createdUser));
    }

    @PreAuthorize("hasAuthority('USER_UPDATE') or (isAuthenticated() and #userId == principal.id)")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> update(@PathVariable Long userId,
                                                  @RequestBody UserRequestDTO requestDTO,
                                                  @AuthenticationPrincipal User currentUser) {
        User updatedUser = userService.update(userId, userMapper.userRequestToUser(requestDTO), currentUser);

        return ResponseEntity.ok(userMapper.userToUserResponseDTO(updatedUser));
    }

    @PreAuthorize("hasAuthority('USER_DELETE') or (isAuthenticated() and #userId == principal.id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}")
    public void deleteUserById(@PathVariable Long userId) {
        userService.deleteUserById(userId);
    }
}
