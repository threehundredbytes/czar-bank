package ru.dreadblade.czarbank.service.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.RoleNotFoundException;
import ru.dreadblade.czarbank.exception.UserEmailAlreadyExists;
import ru.dreadblade.czarbank.exception.UserNotFoundException;
import ru.dreadblade.czarbank.exception.UserUsernameAlreadyExists;
import ru.dreadblade.czarbank.repository.security.RoleRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User doesn't exist"));
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserUsernameAlreadyExists("User with username \"" + user.getUsername() + "\" already exists");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserUsernameAlreadyExists("User with email \"" + user.getEmail() + "\" already exists");
        }

        user.setUserId(RandomStringUtils.randomAlphanumeric(10));
        user.setRoles(Collections.singleton(roleService.findRoleByName("CLIENT")));

        return userRepository.save(user);
    }

    public User update(Long userId, User user) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User doesn't exist"));

        String username = user.getUsername();

        if (StringUtils.isNotBlank(username)) {
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setUsername(username);
            } else {
                throw new UserUsernameAlreadyExists("User with username \"" + username + "\" already exists");
            }
        }

        String email = user.getEmail();

        if (StringUtils.isNotBlank(email)) {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setEmail(email);
            } else {
                throw new UserEmailAlreadyExists("User with email \"" + email + "\" already exists");
            }
        }

        Set<Role> roles = user.getRoles().stream()
                .filter(r -> roleRepository.existsByName(r.getName()))
                .map(r -> roleRepository.findByName(r.getName()).get())
                .collect(Collectors.toSet());

        if (roles.size() > 0) {
            userToUpdate.setRoles(roles);
        }

        return userRepository.save(userToUpdate);
    }

    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User doesn't exist");
        }

        userRepository.deleteById(userId);
    }
}
