package com.b2g.reviewservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class remoteJwtService {
    @Value("${authService.internal.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate=new RestTemplate();

    /**
     * Fa la chiamata HTTP al vero auth-service per validare il token
     */
    public Claims remoteValidateToken(String token) {
        System.out.println("called remoteValidateToken with token: " + token);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        if(!authServiceUrl.contains("/validate")) {
            authServiceUrl = authServiceUrl.concat("/validate");
        }
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // L'URL punta all'endpoint interno di auth-service
        ResponseEntity<Map> response = restTemplate.exchange(
                authServiceUrl,
                HttpMethod.GET,
                request,
                Map.class
        );

        // Converte la mappa in Claims di JJWT
        if (response.getStatusCode()== HttpStatus.OK) {
            return Jwts.claims(response.getBody());
        }
        else
            throw new SessionAuthenticationException(response.getBody().toString());

    }

    public UUID extractUserUUID(Claims claims) {
        return UUID.fromString(claims.get("userUUID").toString());
    }
}