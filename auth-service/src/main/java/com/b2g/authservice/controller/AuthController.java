package com.b2g.authservice.controller;

import com.b2g.authservice.dto.LoginRequest;
import com.b2g.authservice.dto.RefreshRequest;
import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.dto.TokenResponse;
import com.b2g.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
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
    public ResponseEntity<?> oauth2Callback(OAuth2AuthenticationToken authentication) {
        Map<String, Object> userInfo = authentication.getPrincipal().getAttributes();
        return ResponseEntity.ok(userInfo);
    }



    @GetMapping("/oauth2/error")
    public ResponseEntity<?> oauth2Error() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("OAuth2 authentication failed");
    }
}