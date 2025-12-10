package com.b2g.authservice.service;
import com.b2g.authservice.exception.InvalidTokenException;
import com.b2g.authservice.model.RefreshToken;
import com.b2g.authservice.model.TokenPair;
import com.b2g.authservice.model.User;
import com.b2g.authservice.repository.RefreshTokenRepository;
import com.b2g.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${jwt.expiration.refresh-token:2592000000}")
    private long refreshTokenExpiration;

    public TokenPair generateTokens(User user) {
        String accessToken = jwtService.generateToken(user.getId().toString(), Map.of(
                "roles", user.getRoles().stream().map(Enum::name).toList(),
                "username", user.getUsername(),
                "userUUID", user.getId().toString()
        ));

        RefreshToken refreshToken = createRefreshToken(user);

        return new TokenPair(accessToken, refreshToken.getToken());
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plus(refreshTokenExpiration, ChronoUnit.MILLIS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public User consumeRefreshToken(String tokenValue) {
        RefreshToken token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new InvalidTokenException("Refresh token expired");
        }

        refreshTokenRepository.delete(token);
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        return user;
    }

    public void revokeRefreshToken(String tokenValue) {
        refreshTokenRepository.deleteByToken(tokenValue);
    }
}