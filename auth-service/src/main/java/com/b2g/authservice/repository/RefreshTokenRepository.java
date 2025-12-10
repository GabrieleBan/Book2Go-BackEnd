package com.b2g.authservice.repository;



import com.b2g.authservice.model.RefreshToken;
import com.b2g.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findByUserId(UUID userId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}