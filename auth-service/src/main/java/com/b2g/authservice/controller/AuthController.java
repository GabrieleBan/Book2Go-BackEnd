package com.b2g.authservice.controller;

import com.b2g.authservice.dto.LoginRequest;
import com.b2g.authservice.dto.RefreshRequest;
import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.dto.TokenResponse;
import com.b2g.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


/*
POST /auth/login → verify credentials, return access + refresh token.

POST /auth/refresh → issue new access token using refresh token.

POST /auth/logout → invalidate refresh token (optional for JWT access tokens).

POST /auth/register → create new user (if self-signup is allowed).

GET /.well-known/jwks.json → expose public keys for JWT validation (if using asymmetric keys).

*/

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OAuth2AuthorizedClientService clientService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        return authService.registerUser(request);
    }

    @PostMapping("/confirm/{token}")
    public ResponseEntity<?> confirmEmail(@PathVariable String token) {
        return authService.confirmEmail(token);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refreshAccessToken(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshRequest request) {
        return authService.logout(request.getRefreshToken());
    }

    @GetMapping("/oauth2/callback")
    public ResponseEntity<TokenResponse> oauth2Callback(OAuth2AuthenticationToken authentication, HttpServletRequest request) {
        Map<String, Object> userInfo = authentication.getPrincipal().getAttributes();
        // Checks that the authentication is from Google or GitHub using the registrationId
        if (!authentication.getAuthorizedClientRegistrationId().equals("google") &&
                !authentication.getAuthorizedClientRegistrationId().equals("github")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Estraggo le informazioni necessarie dall'authentication token
        String email = (String) userInfo.get("email");
//        String name = (String) userInfo.get("name");
//        String googleId = (String) userInfo.get("sub"); // Google ID univoco

        // If email is missing (GitHub case), fetch it via /user/emails
        if (email == null && authentication.getAuthorizedClientRegistrationId().equals("github")) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                    authentication.getAuthorizedClientRegistrationId(),
                    authentication.getName()
            );

            WebClient webClient = WebClient.builder()
                    .baseUrl("https://api.github.com")
                    .defaultHeader(HttpHeaders.AUTHORIZATION,
                            "Bearer " + client.getAccessToken().getTokenValue())
                    .build();

            List<Map<String, Object>> emails = webClient.get()
                    .uri("/user/emails")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            // Find primary verified email
            email = emails.stream()
                    .filter(e -> Boolean.TRUE.equals(e.get("primary")))
                    .map(e -> (String) e.get("email"))
                    .findFirst()
                    .orElse(null);
        }

        // Controllo che l'email sia presente
        if (email == null) {
            return ResponseEntity.badRequest().build();
        }

        // La nostra architettura è stateless, ma Spring Security ha creato una sessione per gestire l'OAuth2
        // Questa permette di evitare attacchi di replay, quindi la invalidiamo subito
        HttpSession oauth2Session = request.getSession(false);
        if(oauth2Session != null) {
            oauth2Session.invalidate(); // Invalida la sessione OAuth2 per evitare replay
        }

        // Utilizzo il metodo loginOauth2 del service
        return authService.loginOauth2(email);
    }



    @GetMapping("/oauth2/error")
    public ResponseEntity<?> oauth2Error() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("OAuth2 authentication failed");
    }
}