package com.b2g.recomendationservice.service;

import com.b2g.recomendationservice.dto.BookSummaryDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class RecommendationService {
    public List<BookSummaryDTO> getGenericReccomendation(Set<UUID> categoryIds) {
        return List.of();
    }

    public List<BookSummaryDTO> getPersonalizedReccomendation(UUID userId, Set<UUID> categoryIds) {
        return List.of();
    }
}
