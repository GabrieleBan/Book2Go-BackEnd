package com.b2g.authservice.service;

import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.*;
import java.security.spec.*;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Getter
    private PublicKey publicKey; // Expose to other microservices

    private PrivateKey privateKey;

    @Value("${jwt.keys.directory:./keys}")
    private String keysDirectory;

    @Value("${jwt.expiration.access-token:900000}")
    private long accessTokenExpiration;

    @Value("${jwt.issuer:}")
    private String issuer;

    @Value("${jwt.audience:}")
    private String audience;

    private static final String PRIVATE_KEY_FILE = "private_key.der";
    private static final String PUBLIC_KEY_FILE = "public_key.der";

    @PostConstruct
    private void initKeys() {
        try {
            Path keysDirPath = Paths.get(keysDirectory);
            Path privateKeyPath = keysDirPath.resolve(PRIVATE_KEY_FILE);
            Path publicKeyPath = keysDirPath.resolve(PUBLIC_KEY_FILE);

            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                // Load existing keys
                loadKeysFromFile(privateKeyPath, publicKeyPath);
                log.info("Loaded existing RSA keys from {}", keysDirectory);
            } else {
                // Generate new keys and save them
                generateAndSaveKeys(keysDirPath, privateKeyPath, publicKeyPath);
                log.info("Generated new RSA keys and saved to {}", keysDirectory);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize RSA key pair", e);
        }
    }

    private void loadKeysFromFile(Path privateKeyPath, Path publicKeyPath) throws Exception {
        // Load private key
        byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Load public key
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = keyFactory.generatePublic(publicKeySpec);
    }

    private void generateAndSaveKeys(Path keysDirPath, Path privateKeyPath, Path publicKeyPath) throws Exception {
        // Create directory if it doesn't exist
        Files.createDirectories(keysDirPath);

        // Generate key pair
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();

        // Save private key
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
        Files.write(privateKeyPath, privateKeySpec.getEncoded());

        // Save public key
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        Files.write(publicKeyPath, publicKeySpec.getEncoded());

        // Set appropriate file permissions (Unix systems)
        try {
            Files.setPosixFilePermissions(privateKeyPath,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            Files.setPosixFilePermissions(publicKeyPath,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException e) {
            // Windows doesn't support POSIX permissions, ignore
            log.warn("Could not set POSIX file permissions (likely Windows environment)");
        }
    }


    public String generateToken(String subject, Map<String, Object> claims) {
        JwtBuilder builder = Jwts.builder()
                .header().add("kid", "auth-key-1").and()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(privateKey); // RS256 inferred from privateKey

        // Add issuer if configured
        if (issuer != null && !issuer.trim().isEmpty()) {
            builder.issuer(issuer);
        }

        // Add audience if configured
        if (audience != null && !audience.trim().isEmpty()) {
            builder.audience().add(audience);
        }

        return builder.compact();
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