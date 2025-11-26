package com.b2g.catalogservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class remoteJwtService {
    @Value("${authService.internal.url}")
    private  String authServiceUrl;

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

    public List<String> extractRoles(Claims claims) {
        List<String> roles = new ArrayList<>();

        // Try to get roles from "roles" claim first (plural)
        Object rolesClaim = claims.get("roles");
        if (rolesClaim != null) {
            if (rolesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesClaim;
                roles.addAll(rolesList);
            } else if (rolesClaim instanceof String) {
                // Single role as string
                roles.add((String) rolesClaim);
            }
        }

        // Also try "role" claim (singular)
        Object roleClaim = claims.get("role");
        if (roleClaim != null) {
            if (roleClaim instanceof String) {
                roles.add((String) roleClaim);
            } else if (roleClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) roleClaim;
                roles.addAll(rolesList);
            }
        }

        // Also try "authorities" claim (common in Spring Security)
        Object authoritiesClaim = claims.get("authorities");
        if (authoritiesClaim != null) {
            if (authoritiesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> authoritiesList = (List<Object>) authoritiesClaim;
                for (Object authority : authoritiesList) {
                    if (authority instanceof String) {
                        roles.add((String) authority);
                    } else if (authority instanceof Map) {
                        // Handle Spring Security GrantedAuthority format: {"authority": "ROLE_ADMIN"}
                        @SuppressWarnings("unchecked")
                        Map<String, Object> authorityMap = (Map<String, Object>) authority;
                        Object authorityValue = authorityMap.get("authority");
                        if (authorityValue instanceof String) {
                            roles.add((String) authorityValue);
                        }
                    }
                }
            }
        }

        return roles;
    }

}