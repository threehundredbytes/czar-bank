package ru.dreadblade.czarbank.service.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));
    }

    public User createUser(User userToCreate, User currentUser) {
        if (userRepository.existsByUsername(userToCreate.getUsername())) {
            throw new CzarBankException(ExceptionMessage.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(userToCreate.getEmail())) {
            throw new CzarBankException(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS);
        }

        userToCreate.setUserId(RandomStringUtils.randomAlphanumeric(10));

        Set<Role> roles = userToCreate.getRoles();

        if (currentUser != null && currentUser.hasAuthority("USER_CREATE") && roles != null && !roles.isEmpty()) {
            roles = roles.stream()
                    .filter(r -> roleRepository.existsByName(r.getName()))
                    .map(r -> roleRepository.findByName(r.getName()).orElseThrow())
                    .collect(Collectors.toSet());

            userToCreate.setRoles(roles);
        } else {
            userToCreate.setRoles(Collections.emptySet());
        }

        String encodedPassword = passwordEncoder.encode(userToCreate.getPassword());

        userToCreate.setPassword(encodedPassword);

        return userRepository.save(userToCreate);
    }

    public User update(Long userId, User updatedUser, User currentUser) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));

        String username = updatedUser.getUsername();

        if (StringUtils.isNotBlank(username)) {
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setUsername(username);
            } else {
                throw new CzarBankException(ExceptionMessage.USERNAME_ALREADY_EXISTS);
            }
        }

        String email = updatedUser.getEmail();

        if (StringUtils.isNotBlank(email)) {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setEmail(email);
            } else {
                throw new CzarBankException(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS);
            }
        }

        Set<Role> roles = updatedUser.getRoles().stream()
                .filter(r -> roleRepository.existsByName(r.getName()))
                .map(r -> roleRepository.findByName(r.getName()).orElseThrow())
                .collect(Collectors.toSet());

        if (roles.size() > 0 && currentUser.hasAuthority("USER_UPDATE")) {
            userToUpdate.setRoles(roles);
        }

        return userRepository.save(userToUpdate);
    }

    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CzarBankException(ExceptionMessage.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }
}
