package ru.dreadblade.czarbank.service.security;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.security.UserRequestDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.UserEmailAlreadyExists;
import ru.dreadblade.czarbank.exception.UserNotFoundException;
import ru.dreadblade.czarbank.exception.UserUsernameAlreadyExists;
import ru.dreadblade.czarbank.repository.security.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        return userRepository.save(user);
    }

    public User update(Long userId, UserRequestDTO requestDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User doesn't exist"));

        String username = requestDTO.getUsername();

        if (StringUtils.isNotBlank(username)) {
            Optional<User> optionalUser = userRepository.findByUsername(username);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                user.setUsername(username);
            } else {
                throw new UserUsernameAlreadyExists("User with username \"" + username + "\" already exists");
            }
        }

        String email = requestDTO.getEmail();

        if (StringUtils.isNotBlank(email)) {
            Optional<User> optionalUser = userRepository.findByEmail(email);

            if (optionalUser.isEmpty() || optionalUser.get().getId().equals(userId)) {
                user.setEmail(email);
            } else {
                throw new UserEmailAlreadyExists("User with email \"" + email + "\" already exists");
            }
        }

        return userRepository.save(user);
    }

    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User doesn't exist");
        }

        userRepository.deleteById(userId);
    }
}
