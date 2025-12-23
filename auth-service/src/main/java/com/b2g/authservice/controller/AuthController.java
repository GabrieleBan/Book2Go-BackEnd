package com.b2g.authservice.controller;

import com.b2g.authservice.dto.LoginRequest;
import com.b2g.authservice.dto.RefreshRequest;
import com.b2g.authservice.dto.SignupRequest;
import com.b2g.authservice.dto.TokenResponse;
import com.b2g.authservice.service.AuthApplicationService;
import com.b2g.authservice.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
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
@CrossOrigin("http://localhost:5173/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthApplicationService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        return authService.registerUser(request);
    }

    @GetMapping("/confirm/{token}")
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
        // La nostra architettura è stateless, ma Spring Security ha creato una sessione per gestire l'OAuth2
        // Questa permette di effettuare attacchi di replay, quindi la invalidiamo subito
        HttpSession oauth2Session = request.getSession(false);
        if(oauth2Session != null) {
            oauth2Session.invalidate(); // Invalida la sessione OAuth2 per evitare replay
        }

        // Utilizzo il metodo loginOauth2 del service
        return authService.loginOauth2(authentication);
    }



    @GetMapping("/oauth2/error")
    public ResponseEntity<?> oauth2Error() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("OAuth2 authentication failed");
    }
    private final JwtService jwtService;
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<?> wellKnownJwks() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) jwtService.getPublicKey();

        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", "auth-key-1");
        jwk.put("n", base64UrlEncode(rsaPublicKey.getModulus()));
        jwk.put("e", base64UrlEncode(rsaPublicKey.getPublicExponent()));

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", List.of(jwk));

        return ResponseEntity.ok(jwks);
    }

    private String base64UrlEncode(BigInteger value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.toByteArray());
    }


}