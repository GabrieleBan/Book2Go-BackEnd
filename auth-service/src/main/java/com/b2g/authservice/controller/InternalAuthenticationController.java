package com.b2g.authservice.controller;

import com.b2g.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(InternalAuthenticationController.class);
    private final JwtService jwtService; // il vero servizio auth, quello che conosce le chiavi

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        String token = authHeader.substring(7);
        System.out.println("token: " + token);

        try {
            Claims claims = jwtService.validateToken(token); // valida localmente
            // converte Claims in Map per restituire JSON
            Map<String, Object> claimsMap = new HashMap<>(claims);
            log.info("claimsMap: " + claimsMap);

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