package com.b2g.recomendationservice.dto;

import java.util.List;
import java.util.UUID;

public record BookSummaryDTO(
        UUID id,
        String title,
        String author,
        String coverImageUrl,
        List<CategoryDTO> categories
) {}
