package com.b2g.authservice.service;

import com.b2g.authservice.dto.LoginRequest;
import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.dto.TokenResponse;
import com.b2g.authservice.exception.AccountNotEnabledException;
import com.b2g.authservice.exception.InvalidCredentialsException;
import com.b2g.authservice.exception.InvalidTokenException;
import com.b2g.authservice.model.*;
import com.b2g.authservice.repository.RefreshTokenRepository;
import com.b2g.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthApplicationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRegistrationService userRegistrationService;
    private final OAuthUserService oauthUserService;
    private final TokenService tokenService;

    /** Caso d'uso: registrazione di un nuovo utente */
    @Transactional
    public ResponseEntity<String> registerUser(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent() ||
                userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username or email already in use");
        }
        User user = userRegistrationService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    /** Caso d'uso: conferma email */
    @Transactional
    public ResponseEntity<String> confirmEmail(String token) {
        User user = userRepository.findById(UUID.fromString(token))
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body("Token already used, invalid or expired");
        }

        userRegistrationService.confirmEmail(user);
        return ResponseEntity.ok("Email confirmed successfully");
    }

    /** Caso d'uso: login con username/password */
    public ResponseEntity<TokenResponse> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
        if (!(user.verifyPassword(request.getPassword()))) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        if (!user.isEnabled()) {
            throw new AccountNotEnabledException("Account not enabled");
        }

        TokenPair tokens = tokenService.generateTokens(user);
        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    /** Caso d'uso: login OAuth2 */
    @Transactional
    public ResponseEntity<TokenResponse> loginOauth2(OAuth2AuthenticationToken authentication) {
        if (!oauthUserService.isValidProvider(authentication.getAuthorizedClientRegistrationId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = oauthUserService.findOrCreateUser(authentication);
        TokenPair tokens = tokenService.generateTokens(user);

        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    /** Caso d'uso: refresh token */
    @Transactional
    public ResponseEntity<TokenResponse> refreshAccessToken(String rawRefreshToken) {
        User user = tokenService.consumeRefreshToken(rawRefreshToken);
        TokenPair tokens = tokenService.generateTokens(user);

        return ResponseEntity.ok(new TokenResponse(tokens.getAccessToken(), tokens.getRefreshToken()));
    }

    /**
     * Caso d'uso: logout */
    @Transactional
    public ResponseEntity<Void> logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok().build();
    }
}