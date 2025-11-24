package com.b2g.authservice.controller;

import com.b2g.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/internal") // solo interno tra servizi
@RequiredArgsConstructor
public class InternalAuthenticationController {

    private final JwtService jwtService; // il vero servizio auth, quello che conosce le chiavi

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        String token = authHeader.substring(7);
        System.out.println("token: " + token);

        try {
            Claims claims = jwtService.validateToken(token); // valida localmente
            // converte Claims in Map per restituire JSON
            Map<String, Object> claimsMap = new HashMap<>(claims);

            return ResponseEntity.ok(claimsMap);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Token expired"));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token"));
        }
    }
}