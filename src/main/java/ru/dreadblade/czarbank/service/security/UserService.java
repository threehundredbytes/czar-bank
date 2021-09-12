package ru.dreadblade.czarbank.service.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.*;
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
        return userRepository.findById(userId).orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));
    }

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new CzarBankException(ExceptionMessage.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new CzarBankException(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS);
        }

        user.setUserId(RandomStringUtils.randomAlphanumeric(10));
        user.setRoles(Collections.singleton(roleService.findRoleByName("CLIENT")));

        return userRepository.save(user);
    }

    public User update(Long userId, User user) {
        User userToUpdate = userRepository.findById(userId).orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));

        String username = user.getUsername();

        if (StringUtils.isNotBlank(username)) {
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setUsername(username);
            } else {
                throw new CzarBankException(ExceptionMessage.USERNAME_ALREADY_EXISTS);
            }
        }

        String email = user.getEmail();

        if (StringUtils.isNotBlank(email)) {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                userToUpdate.setEmail(email);
            } else {
                throw new CzarBankException(ExceptionMessage.USER_EMAIL_ALREADY_EXISTS);
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
            throw new CzarBankException(ExceptionMessage.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }
}
