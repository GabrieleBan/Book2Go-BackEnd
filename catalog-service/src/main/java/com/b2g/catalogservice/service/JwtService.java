package com.b2g.catalogservice.service;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.security.*;
import java.security.spec.*;

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
}