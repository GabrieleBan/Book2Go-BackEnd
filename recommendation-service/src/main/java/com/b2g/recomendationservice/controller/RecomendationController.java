package com.b2g.recomendationservice.controller;

import com.b2g.recomendationservice.annotations.RequireUserUUID;
import com.b2g.recomendationservice.dto.BookSummaryDTO;
import com.b2g.recomendationservice.service.JwtService;
import com.b2g.recomendationservice.service.RecommendationService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecomendationController {
    private final JwtService jwtService;
    private final RecommendationService recommService;
    @GetMapping({"", "/"})
    public ResponseEntity<?> genericRecommendation(
            @RequestParam(required = false) Set<UUID> categoryIds) {
        List<BookSummaryDTO> recommendationList = recommService.getGenericReccomendation(categoryIds);
        return ResponseEntity.status(HttpStatus.OK).body(recommendationList);
    }
    @GetMapping("/personalized")
    @RequireUserUUID
    public ResponseEntity<?> personalizedRecommendation(
            @RequestParam(required = false) Set<UUID> categoryIds,
            @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7);
        if (jwt.trim().isEmpty()) {
            log.warn("Empty JWT token for role-protected endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid token");
        }

        // Validate JWT and extract claims
        Claims claims = jwtService.validateToken(jwt);
        UUID userId= jwtService.extractUserUUID(claims);
        List<BookSummaryDTO> recommendationList = recommService.getPersonalizedReccomendation(userId, categoryIds);
        return ResponseEntity.status(HttpStatus.OK).body(recommendationList);
    }

}
