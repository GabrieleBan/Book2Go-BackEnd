package com.b2g.authservice.service;

import com.b2g.authservice.dto.LoginRequest;
import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.dto.TokenResponse;
import com.b2g.authservice.model.OAuthProvider;
import com.b2g.authservice.model.RefreshToken;
import com.b2g.authservice.model.User;
import com.b2g.authservice.model.UserRole;
import com.b2g.authservice.repository.RefreshTokenRepository;
import com.b2g.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Constants
    private static final String GOOGLE_PROVIDER = "google";
    private static final String GITHUB_PROVIDER = "github";
    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private static final String USER_EMAILS_ENDPOINT = "/user/emails";

    // Error messages
    private static final String USER_EXISTS_ERROR = "Username or email already in use";
    private static final String INVALID_TOKEN_ERROR = "Invalid token";
    private static final String TOKEN_USED_ERROR = "Token already used, invalid or expired";
    private static final String REGISTRATION_SUCCESS = "User registered successfully";
    private static final String EMAIL_CONFIRMED_SUCCESS = "Email confirmed successfully";
    private static final String INVALID_CREDENTIALS_ERROR = "Invalid username or password";
    private static final String INVALID_REFRESH_TOKEN_ERROR = "Invalid refresh token";

    @Value("${jwt.expiration.refresh-token:2592000000}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OAuth2AuthorizedClientService clientService;

    @Transactional
    public ResponseEntity<?> registerUser(SignupRequest request) {
        if (userExists(request.getEmail(), request.getUsername())) {
            return ResponseEntity.badRequest().body(USER_EXISTS_ERROR);
        }

        User user = createUnconfirmedUser(request);
        userRepository.save(user);

        // TODO: implement email sending (the token is the user UUID)
        return ResponseEntity.ok(REGISTRATION_SUCCESS);
    }

    @Transactional
    public ResponseEntity<?> confirmEmail(String token) {
        User user = findUserByToken(token);

        if (user.isEnabled()) {
            return ResponseEntity.badRequest().body(TOKEN_USED_ERROR);
        }

        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok(EMAIL_CONFIRMED_SUCCESS);
    }

    @Transactional
    public ResponseEntity<TokenResponse> loginOauth2(OAuth2AuthenticationToken authentication) {
        if (!isValidOAuthProvider(authentication.getAuthorizedClientRegistrationId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = extractEmailFromAuthentication(authentication);
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = findOrCreateOAuthUser(email, authentication.getAuthorizedClientRegistrationId());
        return generateTokenResponse(user);
    }

    public ResponseEntity<TokenResponse> login(LoginRequest request) {
        User user = validateUserCredentials(request);
        return generateTokenResponse(user);
    }

    public ResponseEntity<TokenResponse> refreshAccessToken(String refreshToken) {
        RefreshToken token = validateRefreshToken(refreshToken);
        User user = token.getUser();

        // Clean up old token and create new one
        refreshTokenRepository.delete(token);
        return generateTokenResponse(user);
    }

    @Transactional
    public ResponseEntity<Void> logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
        return ResponseEntity.ok().build();
    }

    // Private helper methods

    @Transactional(readOnly = true)
    protected boolean userExists(String email, String username) {
        return userRepository.findByEmail(email).isPresent() ||
                userRepository.findByUsername(username).isPresent();
    }

    private User createUnconfirmedUser(SignupRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .authProvider(OAuthProvider.NONE)
                .role(UserRole.USER)
                .build();
    }

    private User findUserByToken(String token) {
        return userRepository.findById(UUID.fromString(token))
                .orElseThrow(() -> new RuntimeException(INVALID_TOKEN_ERROR));
    }

    private boolean isValidOAuthProvider(String registrationId) {
        return GOOGLE_PROVIDER.equals(registrationId) || GITHUB_PROVIDER.equals(registrationId);
    }

    private String extractEmailFromAuthentication(OAuth2AuthenticationToken authentication) {
        Map<String, Object> userInfo = authentication.getPrincipal().getAttributes();
        String email = (String) userInfo.get("email");

        // Handle GitHub case where email might not be directly available
        if (email == null && GITHUB_PROVIDER.equals(authentication.getAuthorizedClientRegistrationId())) {
            email = fetchGitHubPrimaryEmail(authentication);
        }

        return email;
    }

    private String fetchGitHubPrimaryEmail(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        WebClient webClient = WebClient.builder()
                .baseUrl(GITHUB_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "Bearer " + client.getAccessToken().getTokenValue())
                .build();

        List<Map<String, Object>> emails = webClient.get()
                .uri(USER_EMAILS_ENDPOINT)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        return emails.stream()
                .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                .map(e -> (String) e.get("email"))
                .findFirst()
                .orElse(null);
    }

    private User findOrCreateOAuthUser(String email, String registrationId) {
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return updateExistingOAuthUser(existingUser.get(), registrationId);
        } else {
            return createNewOAuthUser(email, registrationId);
        }
    }

    private User updateExistingOAuthUser(User user, String registrationId) {
        boolean needsUpdate = false;

        if (!user.isEnabled()) {
            user.setEnabled(true);
            needsUpdate = true;
        }

        if (user.getAuthProvider() == OAuthProvider.NONE) {
            user.setAuthProvider(getOAuthProvider(registrationId));
            needsUpdate = true;
        }

        if (needsUpdate) {
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        return user;
    }

    private User createNewOAuthUser(String email, String registrationId) {
        User user = User.builder()
                .username(generateUsernameFromEmail(email))
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .enabled(true)
                .authProvider(getOAuthProvider(registrationId))
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    private OAuthProvider getOAuthProvider(String registrationId) {
        return GOOGLE_PROVIDER.equals(registrationId) ? OAuthProvider.GOOGLE : OAuthProvider.GITHUB;
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private User validateUserCredentials(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(INVALID_CREDENTIALS_ERROR));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException(INVALID_CREDENTIALS_ERROR);
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not enabled");
        }

        return user;
    }

    private RefreshToken validateRefreshToken(String refreshTokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException(INVALID_REFRESH_TOKEN_ERROR));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    private ResponseEntity<TokenResponse> generateTokenResponse(User user) {
        String accessToken = createAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user);

        TokenResponse response = TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();

        return ResponseEntity.ok(response);
    }

    private String createAccessToken(User user) {
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "username", user.getUsername()
        );
        return jwtService.generateToken(user.getId().toString(), claims);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(System.currentTimeMillis() + refreshTokenExpiration),
                        ZoneId.systemDefault()
                ))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}