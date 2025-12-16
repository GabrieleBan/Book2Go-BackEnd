package com.b2g.lendservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
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

    private final RestTemplate restTemplate;

    /**
     * Fa la chiamata HTTP al vero auth-service per validare il token
     */
    public Claims remoteValidateToken(String token) {

        String validateUrl = authServiceUrl.endsWith("/validate")
                ? authServiceUrl
                : authServiceUrl + "/validate";
        log.info("Validating token: " + token);
        log.info("Validating url: " + validateUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    validateUrl,
                    HttpMethod.GET,
                    request,
                    Map.class
            );

            return Jwts.claims(response.getBody());

        } catch (HttpClientErrorException.Unauthorized e) {
            throw new SessionAuthenticationException("Token invalid or expired");

        } catch (RestClientException e) {
            log.error("Invalid token", e);
            throw new SessionAuthenticationException("Token invalid or expired");
        }
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