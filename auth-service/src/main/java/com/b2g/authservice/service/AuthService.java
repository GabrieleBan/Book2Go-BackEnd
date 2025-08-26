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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    @Transactional
    public ResponseEntity<?> registerUser(SignupRequest request) {
        //Controlliamo che l'utente non esista già
        if(userRepository.findByEmail(request.getEmail()).isPresent()
        || userRepository.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body("Username or email already in use");
        }

        //Salviamo l'utente nel db come non confermato
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(false)
                .authProvider(OAuthProvider.NONE)
                .role(UserRole.USER)
                .build();

        //Mandiamo l'email di conferma
        //TODO: implement email sending (the token is the user UUID)

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @Transactional
    public ResponseEntity<?> confirmEmail(String token) {
        //Cerchiamo l'utente con quel token
        User user = userRepository.findById(java.util.UUID.fromString(token))
                .orElseThrow(() -> new RuntimeException("Invalid token"));


        //Se è già abilitato, inviamo come risposta che il token è già stato usato oppure che è scaduto
        if(user.isEnabled()){
            return ResponseEntity.badRequest().body("Token already used, invalid or expired");
        }

        //Se lo troviamo, lo abilitiamo
        user.setEnabled(true);
        userRepository.save(user);

        return ResponseEntity.ok("Email confirmed successfully");
    }

    @Transactional
    public ResponseEntity<TokenResponse> loginOauth2(String email) {
        // Controllo se l'utente con questa email esiste già nel DB
        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            // Se esiste, lo loggo direttamente
            user = existingUser.get();

            // Se l'utente esiste ma non è abilitato, lo abilito direttamente
            if (!user.isEnabled()) {
                user.setEnabled(true);
            }

            // Se l'utente esiste ma il provider è NONE, lo aggiorno a GOOGLE
            if (user.getAuthProvider() == OAuthProvider.NONE) {
                user.setAuthProvider(OAuthProvider.GOOGLE);
            }

            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        } else {
            // Se non esiste, lo registro come utente con ruolo USER e provider GOOGLE
            // Lo imposto però come abilitato direttamente, senza conferma email
            user = User.builder()
                    .username(generateUsernameFromEmail(email))
                    .email(email)
                    .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString())) // Password casuale per OAuth2
                    .enabled(true) // Abilitato direttamente
                    .authProvider(OAuthProvider.GOOGLE)
                    .role(UserRole.USER)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
        }

        // In tutti i casi, procedo come nel login standard, generando access e refresh token
        return generateTokensForUser(user);
    }

    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        String username = baseUsername;
        int counter = 1;

        // Assicuriamoci che l'username sia unico
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    private ResponseEntity<TokenResponse> generateTokensForUser(User user) {
        // Generiamo il token JWT
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "username", user.getUsername()
        );
        String jwtToken = jwtService.generateToken(user.getId().toString(), claims, 1000 * 60 * 15);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 7L),
                        ZoneId.systemDefault()
                ))
                .build();

        refreshTokenRepository.save(refreshToken);

        TokenResponse response = TokenResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TokenResponse> login(LoginRequest request) {
        //Cerchiamo l'utente con quell'username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        //Controlliamo che la password sia corretta
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            return ResponseEntity.badRequest().build();
        }

        //Controlliamo che l'utente sia abilitato
        if(!user.isEnabled()){
            return ResponseEntity.badRequest().build();
        }

        //Generiamo il token JWT
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "username", user.getUsername()
        );
        String jwtToken = jwtService.generateToken(user.getId().toString(), claims, 1000 * 60 * 15);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate( LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 7L),
                        ZoneId.systemDefault()
                ))
                .build();

        refreshTokenRepository.save(refreshToken);

        TokenResponse response = TokenResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TokenResponse> refreshAccessToken(String refreshToken) {
        //Cerchiamo il refresh token
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        //Controlliamo che non sia scaduto
        if(token.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(token);
            return ResponseEntity.badRequest().build();
        }

        User user = token.getUser();

        //Generiamo il token JWT
        Map<String, Object> claims = Map.of(
                "role", user.getRole().name(),
                "username", user.getUsername()
        );
        String jwtToken = jwtService.generateToken(user.getId().toString(), claims, 1000 * 60 * 15);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate( LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(System.currentTimeMillis() + 1000L * 60L * 60L * 24L * 7L),
                        ZoneId.systemDefault()
                ))
                .build();

        TokenResponse response = TokenResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(newRefreshToken.getToken())
                .build();

        refreshTokenRepository.delete(token);
        refreshTokenRepository.save(newRefreshToken);
        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<Void> logout(String refreshToken) {
        //Delete the refresh token from the database if it exists
        refreshTokenRepository.deleteByToken(refreshToken);

        return ResponseEntity.ok().build();
    }
}