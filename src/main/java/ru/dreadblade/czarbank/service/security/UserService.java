package ru.dreadblade.czarbank.service.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.dreadblade.czarbank.domain.security.EmailVerificationToken;
import ru.dreadblade.czarbank.domain.security.Role;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.CzarBankException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;
import ru.dreadblade.czarbank.repository.security.RoleRepository;
import ru.dreadblade.czarbank.repository.security.UserRepository;
import ru.dreadblade.czarbank.service.email.MailService;
import ru.dreadblade.czarbank.service.freemarker.FreemarkerTemplateService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String VERIFICATION_EMAIL_SUBJECT = "czar-bank account verification";
    private static final String VERIFICATION_EMAIL_SUPPORT_EMAIL_ADDRESS = "support@czarbank.org";
    private static final String VERIFICATION_EMAIL_TEMPLATE_FILENAME = "verification-email-message.ftlh";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final FreemarkerTemplateService templateService;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new CzarBankException(ExceptionMessage.USER_NOT_FOUND));
    }

    @SneakyThrows
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
                    .filter(r -> roleRepository.existsById(r.getId()))
                    .map(r -> roleRepository.findById(r.getId()).orElseThrow())
                    .collect(Collectors.toSet());

            userToCreate.setRoles(roles);
        } else {
            userToCreate.setRoles(Collections.emptySet());
        }

        String encodedPassword = passwordEncoder.encode(userToCreate.getPassword());

        userToCreate.setPassword(encodedPassword);

        User user = userRepository.save(userToCreate);

        EmailVerificationToken emailVerificationToken = emailVerificationTokenService.generateVerificationToken(user);

        String emailVerificationUrl = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .replacePath("/api/account-management/verify-email/")
                .toUriString() + emailVerificationToken.getEmailVerificationToken();

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("username", user.getUsername());
        templateModel.put("emailVerificationUrl", emailVerificationUrl);
        templateModel.put("supportEmailAddress", VERIFICATION_EMAIL_SUPPORT_EMAIL_ADDRESS);

        String emailMessageContent = templateService.getProcessedFreemarkerTemplate(VERIFICATION_EMAIL_TEMPLATE_FILENAME, templateModel);

        mailService.sendHtmlMail(user.getEmail(), VERIFICATION_EMAIL_SUBJECT, emailMessageContent);

        return user;
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

        Set<Role> roles = updatedUser.getRoles();

        if (currentUser.hasAuthority("USER_UPDATE") && roles != null && !roles.isEmpty()) {
            roles = roles.stream()
                    .filter(r -> roleRepository.existsById(r.getId()))
                    .map(r -> roleRepository.findById(r.getId()).orElseThrow())
                    .collect(Collectors.toSet());

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
