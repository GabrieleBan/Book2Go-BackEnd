package com.b2g.recomendationservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Getter
    private PublicKey publicKey;

    @Value("${jwt.keys.directory:./keys}")
    private String keysDirectory;

    @Value("${jwt.issuer:}")
    private String issuer;

    @Value("${jwt.audience:}")
    private String audience;

    private static final String PUBLIC_KEY_FILE = "public_key.der";

    @PostConstruct
    private void initKeys() {
        try {
            Path keysDirPath = Paths.get(keysDirectory);
            Path publicKeyPath = keysDirPath.resolve(PUBLIC_KEY_FILE);

            if (Files.exists(publicKeyPath)) {
                // Load existing public key
                loadPublicKeyFromFile(publicKeyPath);
                log.info("Loaded existing RSA public key from {}", keysDirectory);
            } else {
                throw new IllegalStateException("Public key file not found at: " + publicKeyPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA public key", e);
        }
    }

    private void loadPublicKeyFromFile(Path publicKeyPath) throws Exception {
        // Load public key
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
    }

    public Claims validateToken(String token) {
        try {
            JwtParserBuilder parserBuilder = Jwts.parser().verifyWith(publicKey);

            // Validate issuer if configured
            if (issuer != null && !issuer.trim().isEmpty()) {
                parserBuilder.requireIssuer(issuer);
            }

            // Validate audience if configured
            if (audience != null && !audience.trim().isEmpty()) {
                parserBuilder.requireAudience(audience);
            }

            return parserBuilder
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // Handle the exception appropriately
            throw new JwtException("Invalid token", e);
        }
    }

    /**
     * Extract roles from JWT claims.
     * Supports both "roles", "role" and "authorities" claim names.
     *
     * @param claims JWT claims
     * @return List of role names
     */
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

    public UUID extractUserUUID(Claims claims){
//        System.out.println(claims.get("userUUID"));

        return (UUID) UUID.fromString(claims.get("userUUID").toString());
    }
}