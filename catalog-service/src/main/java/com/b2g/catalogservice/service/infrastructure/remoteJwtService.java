package com.b2g.catalogservice.service.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class remoteJwtService {

    @Value("${authService.internal.url}")
    private String authServiceUrl;

    @Value("${authService.jwks.url}")
    private String jwksUrl;

    @Value("${jwt.public-key.cache.seconds:300}")
    private long cacheSeconds;

    private final RestTemplate restTemplate;

    private Map<String, RSAPublicKey> keyCache = new ConcurrentHashMap<>();
    private Instant lastFetch = Instant.EPOCH;

    public Claims remoteValidateToken(String token) {
        try {
            String kid = getKid(token);
            RSAPublicKey publicKey = getCachedKey(kid);

            if (publicKey == null) {
                log.info("Public key not found in cache, fallback to auth-service");
                return remoteValidateTokenHttp(token);
            }


            return Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed locally, fallback to auth-service", e);
            return remoteValidateTokenHttp(token);
        }
    }

    private Claims remoteValidateTokenHttp(String token) {
        String validateUrl = authServiceUrl.endsWith("/internal/validate")
                ? authServiceUrl
                : authServiceUrl + "/internal/validate";
        log.info("Validating token via auth-service: {}", validateUrl);

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

    private String getKid(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new io.jsonwebtoken.JwtException("Invalid JWT format");

        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        try {
            Map<String, Object> header = new ObjectMapper().readValue(headerJson, Map.class);
            return header.get("kid").toString();
        } catch (JsonProcessingException e) {
            throw new io.jsonwebtoken.JwtException("Invalid JWT header", e);
        }
    }


    private synchronized RSAPublicKey getCachedKey(String kid) {
        if (keyCache.containsKey(kid) && Instant.now().isBefore(lastFetch.plusSeconds(cacheSeconds))) {
            return keyCache.get(kid);
        }

        try {
            Map<String, Object> jwks = restTemplate.getForObject(jwksUrl, Map.class);
            List<Map<String, String>> keys = (List<Map<String, String>>) jwks.get("keys");
            Map<String, RSAPublicKey> newCache = new HashMap<>();
            for (Map<String, String> key : keys) {
                if (!"RSA".equals(key.get("kty"))) continue;

                BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("n")));
                BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(key.get("e")));

                RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                        .generatePublic(new RSAPublicKeySpec(modulus, exponent));

                newCache.put(key.get("kid"), publicKey);
            }
            keyCache = newCache;
            lastFetch = Instant.now();
            return keyCache.get(kid);
        } catch (Exception e) {
            log.error("Failed to fetch JWKS, using cached key if available", e);
            return keyCache.get(kid);
        }
    }


    public UUID extractUserUUID(Claims claims) {
        return UUID.fromString(claims.get("userUUID").toString());
    }

    public List<String> extractRoles(Claims claims) {
        List<String> roles = new ArrayList<>();

        Object rolesClaim = claims.get("roles");
        if (rolesClaim != null) {
            if (rolesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesClaim;
                roles.addAll(rolesList);
            } else if (rolesClaim instanceof String) {
                roles.add((String) rolesClaim);
            }
        }

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

        Object authoritiesClaim = claims.get("authorities");
        if (authoritiesClaim != null) {
            if (authoritiesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> authoritiesList = (List<Object>) authoritiesClaim;
                for (Object authority : authoritiesList) {
                    if (authority instanceof String) {
                        roles.add((String) authority);
                    } else if (authority instanceof Map) {
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