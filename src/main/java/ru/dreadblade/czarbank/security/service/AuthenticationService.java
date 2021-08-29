package ru.dreadblade.czarbank.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.exception.EntityNotFoundException;
import ru.dreadblade.czarbank.exception.ExceptionMessage;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public User authenticateUser(AuthenticationRequestDTO authenticationRequestDTO) {
        String username = authenticationRequestDTO.getUsername();
        String password = authenticationRequestDTO.getPassword();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManager.authenticate(token);

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        throw new EntityNotFoundException(ExceptionMessage.USER_NOT_FOUND);
    }
}
