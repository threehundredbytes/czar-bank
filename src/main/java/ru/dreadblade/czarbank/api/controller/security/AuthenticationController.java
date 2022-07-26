package ru.dreadblade.czarbank.api.controller.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.dreadblade.czarbank.api.model.request.security.AuthenticationRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.LogoutRequestDTO;
import ru.dreadblade.czarbank.api.model.request.security.RefreshTokensRequestDTO;
import ru.dreadblade.czarbank.api.model.response.security.AuthenticationResponseDTO;
import ru.dreadblade.czarbank.domain.security.User;
import ru.dreadblade.czarbank.security.service.AccessTokenService;
import ru.dreadblade.czarbank.security.service.AuthenticationService;
import ru.dreadblade.czarbank.security.service.RefreshTokenService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequestMapping("/api/auth")
@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, AccessTokenService accessTokenService, RefreshTokenService refreshTokenService) {
        this.authenticationService = authenticationService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {
        User authenticatedUser = authenticationService.authenticateUser(authenticationRequestDTO);

        String accessToken = accessTokenService.generateAccessToken(authenticatedUser);
        String refreshToken = refreshTokenService.generateRefreshToken(authenticatedUser);

        return ResponseEntity.ok(AuthenticationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build());
    }

    @PostMapping("/refresh-tokens")
    public ResponseEntity<AuthenticationResponseDTO> refreshTokens(@Valid @RequestBody RefreshTokensRequestDTO refreshTokensRequestDTO) {
        String refreshToken = refreshTokensRequestDTO.getRefreshToken();

        return ResponseEntity.ok(AuthenticationResponseDTO.builder()
                .accessToken(refreshTokenService.updateAccessToken(refreshToken))
                .refreshToken(refreshTokenService.updateRefreshToken(refreshToken))
                .build());
    }

    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody LogoutRequestDTO logoutRequestDTO,
                       HttpServletRequest request) {
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        String refreshToken = logoutRequestDTO.getRefreshToken();

        authenticationService.logout(accessToken, refreshToken);
    }
}
